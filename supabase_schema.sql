-- ============================================================================
-- RentNear - Supabase PostgreSQL Database Schema & Migration
-- Run this complete script in the Supabase SQL Editor (Dashboard > SQL Editor)
-- ============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. PROPERTIES TABLE (Single Source of Truth for Rental Listings)
CREATE TABLE IF NOT EXISTS public.properties (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL,
    owner_name TEXT NOT NULL DEFAULT '',
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    property_type TEXT NOT NULL DEFAULT 'Flat' CHECK (property_type IN ('House', 'Flat', 'Room', 'Shop', 'Office', 'PG', 'Other')),
    rent INTEGER NOT NULL CHECK (rent >= 0),
    security_deposit INTEGER NOT NULL DEFAULT 0 CHECK (security_deposit >= 0),
    area_sqft INTEGER NOT NULL CHECK (area_sqft > 0),
    bedrooms INTEGER NOT NULL DEFAULT 1 CHECK (bedrooms >= 0),
    bathrooms INTEGER NOT NULL DEFAULT 1 CHECK (bathrooms >= 0),
    address TEXT NOT NULL,
    locality TEXT NOT NULL DEFAULT '',
    city TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    longitude DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    furnishing_status TEXT NOT NULL DEFAULT 'Unfurnished' CHECK (furnishing_status IN ('Furnished', 'Semi-Furnished', 'Unfurnished')),
    has_parking BOOLEAN NOT NULL DEFAULT false,
    water_supply TEXT NOT NULL DEFAULT 'Corporation' CHECK (water_supply IN ('24/7', 'Corporation', 'Borewell', 'Other')),
    has_electricity_backup BOOLEAN NOT NULL DEFAULT false,
    has_balcony BOOLEAN NOT NULL DEFAULT false,
    amenities TEXT[] DEFAULT '{}',
    contact_phone TEXT NOT NULL DEFAULT '',
    whatsapp_number TEXT NOT NULL DEFAULT '',
    listed_by TEXT NOT NULL DEFAULT 'Owner' CHECK (listed_by IN ('Owner', 'Broker')),
    status TEXT NOT NULL DEFAULT 'Available' CHECK (status IN ('Available', 'Rented', 'Sold', 'Hidden')),
    is_featured BOOLEAN NOT NULL DEFAULT false,
    image_urls TEXT[] DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Migration support: add missing columns if upgrading an existing database
ALTER TABLE public.properties ADD COLUMN IF NOT EXISTS owner_name TEXT DEFAULT '';
ALTER TABLE public.properties ADD COLUMN IF NOT EXISTS image_urls TEXT[] DEFAULT '{}';

-- 2. FAVORITES TABLE (Saved properties per user)
CREATE TABLE IF NOT EXISTS public.favorites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    property_id UUID NOT NULL REFERENCES public.properties(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    CONSTRAINT unique_user_property_favorite UNIQUE (user_id, property_id)
);

-- 3. USER PROFILES TABLE (Optional cloud profile sync)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY,
    email TEXT NOT NULL,
    full_name TEXT NOT NULL DEFAULT '',
    phone TEXT DEFAULT '',
    role TEXT NOT NULL DEFAULT 'tenant' CHECK (role IN ('tenant', 'owner', 'admin')),
    avatar_url TEXT DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- ============================================================================
-- HIGH-PERFORMANCE INDEXES
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_properties_city ON public.properties (city);
CREATE INDEX IF NOT EXISTS idx_properties_locality ON public.properties (locality);
CREATE INDEX IF NOT EXISTS idx_properties_type ON public.properties (property_type);
CREATE INDEX IF NOT EXISTS idx_properties_rent ON public.properties (rent);
CREATE INDEX IF NOT EXISTS idx_properties_area ON public.properties (area_sqft);
CREATE INDEX IF NOT EXISTS idx_properties_status ON public.properties (status);
CREATE INDEX IF NOT EXISTS idx_properties_owner ON public.properties (owner_id);
CREATE INDEX IF NOT EXISTS idx_properties_created_at ON public.properties (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_favorites_user ON public.favorites (user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_property ON public.favorites (property_id);

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================================
ALTER TABLE public.properties ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Clean existing policies for idempotence
DROP POLICY IF EXISTS "Public can view available properties" ON public.properties;
DROP POLICY IF EXISTS "Owners can insert properties" ON public.properties;
DROP POLICY IF EXISTS "Users can insert properties" ON public.properties;
DROP POLICY IF EXISTS "Allow public insert of properties" ON public.properties;
DROP POLICY IF EXISTS "Owners can update own properties" ON public.properties;
DROP POLICY IF EXISTS "Owners can delete own properties" ON public.properties;

DROP POLICY IF EXISTS "Users can view favorites" ON public.favorites;
DROP POLICY IF EXISTS "Users can add favorites" ON public.favorites;
DROP POLICY IF EXISTS "Users can delete favorites" ON public.favorites;

DROP POLICY IF EXISTS "Public profiles are viewable by everyone" ON public.profiles;
DROP POLICY IF EXISTS "Users can insert their own profile" ON public.profiles;
DROP POLICY IF EXISTS "Users can update own profile" ON public.profiles;

-- 1. PROPERTIES POLICIES:
-- A. Public can read any available rental listing or owners can view their own listings
CREATE POLICY "Public can view available properties" 
ON public.properties FOR SELECT 
USING (status IN ('Available', 'AVAILABLE') OR auth.uid() = owner_id);

-- B. Valid property listings can be published with required fields
CREATE POLICY "Users can insert properties" 
ON public.properties FOR INSERT 
WITH CHECK (
    owner_id IS NOT NULL AND
    title IS NOT NULL AND title <> '' AND
    city IS NOT NULL AND city <> '' AND
    rent >= 0
);

-- C. Only the owner can update their listings
CREATE POLICY "Owners can update own properties" 
ON public.properties FOR UPDATE 
USING (
    (auth.uid() IS NOT NULL AND auth.uid() = owner_id)
    OR
    (auth.role() = 'anon' AND owner_id IS NOT NULL)
);

-- D. Only the owner can delete their listings
CREATE POLICY "Owners can delete own properties" 
ON public.properties FOR DELETE 
USING (
    (auth.uid() IS NOT NULL AND auth.uid() = owner_id)
    OR
    (auth.role() = 'anon' AND owner_id IS NOT NULL)
);

-- 2. FAVORITES POLICIES:
CREATE POLICY "Users can view favorites" 
ON public.favorites FOR SELECT 
USING (true);

CREATE POLICY "Users can add favorites" 
ON public.favorites FOR INSERT 
WITH CHECK (user_id IS NOT NULL);

CREATE POLICY "Users can delete favorites" 
ON public.favorites FOR DELETE 
USING (true);

-- 3. PROFILES POLICIES:
CREATE POLICY "Public profiles are viewable by everyone" 
ON public.profiles FOR SELECT 
USING (true);

CREATE POLICY "Users can insert their own profile" 
ON public.profiles FOR INSERT 
WITH CHECK (id IS NOT NULL);

CREATE POLICY "Users can update own profile" 
ON public.profiles FOR UPDATE 
USING (auth.uid() = id OR auth.role() = 'anon');

-- ============================================================================
-- REALTIME SUBSCRIPTIONS
-- ============================================================================
-- Allow clients to receive real-time updates when properties are added/changed
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'properties'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.properties;
    END IF;
END $$;
