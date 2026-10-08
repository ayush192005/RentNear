-- ============================================================================
-- RentNear - Supabase Database Schema
-- Run this script in the Supabase SQL Editor to set up tables, RLS & indexes.
-- ============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. PROFILES TABLE
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    full_name TEXT NOT NULL DEFAULT '',
    phone TEXT DEFAULT '',
    role TEXT NOT NULL DEFAULT 'tenant' CHECK (role IN ('tenant', 'owner', 'admin')),
    avatar_url TEXT DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 2. PROPERTIES TABLE
CREATE TABLE IF NOT EXISTS public.properties (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    property_type TEXT NOT NULL CHECK (property_type IN ('House', 'Flat', 'Room', 'Shop', 'Office', 'PG', 'Other')),
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
    contact_phone TEXT NOT NULL,
    whatsapp_number TEXT NOT NULL,
    listed_by TEXT NOT NULL DEFAULT 'Owner' CHECK (listed_by IN ('Owner', 'Broker')),
    status TEXT NOT NULL DEFAULT 'Available' CHECK (status IN ('Available', 'Rented', 'Sold', 'Hidden')),
    is_featured BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 3. PROPERTY IMAGES TABLE
CREATE TABLE IF NOT EXISTS public.property_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id UUID NOT NULL REFERENCES public.properties(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT false,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 4. FAVORITES TABLE
CREATE TABLE IF NOT EXISTS public.favorites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    property_id UUID NOT NULL REFERENCES public.properties(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    CONSTRAINT unique_user_property_favorite UNIQUE (user_id, property_id)
);

-- ============================================================================
-- INDEXES FOR HIGH-PERFORMANCE SEARCH
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_properties_city ON public.properties (city);
CREATE INDEX IF NOT EXISTS idx_properties_locality ON public.properties (locality);
CREATE INDEX IF NOT EXISTS idx_properties_type ON public.properties (property_type);
CREATE INDEX IF NOT EXISTS idx_properties_rent ON public.properties (rent);
CREATE INDEX IF NOT EXISTS idx_properties_area ON public.properties (area_sqft);
CREATE INDEX IF NOT EXISTS idx_properties_status ON public.properties (status);
CREATE INDEX IF NOT EXISTS idx_properties_owner ON public.properties (owner_id);
CREATE INDEX IF NOT EXISTS idx_properties_created_at ON public.properties (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_property_images_property ON public.property_images (property_id);
CREATE INDEX IF NOT EXISTS idx_favorites_user ON public.favorites (user_id);

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================================
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.properties ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.property_images ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.favorites ENABLE ROW LEVEL SECURITY;

-- Profiles: Users can view all public profiles, but only update their own
CREATE POLICY "Public profiles are viewable by everyone" 
ON public.profiles FOR SELECT USING (true);

CREATE POLICY "Users can insert their own profile" 
ON public.profiles FOR INSERT WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can update own profile" 
ON public.profiles FOR UPDATE USING (auth.uid() = id);

-- Properties: Public can view Available properties. Owners can view all their properties.
CREATE POLICY "Public can view available properties" 
ON public.properties FOR SELECT 
USING (status = 'Available' OR auth.uid() = owner_id);

CREATE POLICY "Owners can insert properties" 
ON public.properties FOR INSERT 
WITH CHECK (auth.uid() = owner_id);

CREATE POLICY "Owners can update own properties" 
ON public.properties FOR UPDATE 
USING (auth.uid() = owner_id);

CREATE POLICY "Owners can delete own properties" 
ON public.properties FOR DELETE 
USING (auth.uid() = owner_id);

-- Property Images: Public can view images for visible properties
CREATE POLICY "Images are viewable if property is viewable" 
ON public.property_images FOR SELECT 
USING (EXISTS (
    SELECT 1 FROM public.properties p 
    WHERE p.id = property_id AND (p.status = 'Available' OR p.owner_id = auth.uid())
));

CREATE POLICY "Owners can manage property images" 
ON public.property_images FOR ALL 
USING (EXISTS (
    SELECT 1 FROM public.properties p 
    WHERE p.id = property_id AND p.owner_id = auth.uid()
));

-- Favorites: Users can manage only their own favorites
CREATE POLICY "Users can view own favorites" 
ON public.favorites FOR SELECT 
USING (auth.uid() = user_id);

CREATE POLICY "Users can add own favorites" 
ON public.favorites FOR INSERT 
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own favorites" 
ON public.favorites FOR DELETE 
USING (auth.uid() = user_id);
