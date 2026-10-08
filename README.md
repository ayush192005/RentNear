# 🏠 RentNear

> Find houses, flats, rooms, shops, offices, and other rental properties near you.

RentNear is a rental-property discovery platform designed to make finding rental properties easier, faster, and more convenient.

Users can explore available properties, view important details, check locations, compare options, save properties, and contact property owners.

## ✨ Features

- 🏠 Browse rental properties
- 🔎 Search by location and property type
- 📍 View property location
- 💰 View rental price
- 📐 View property size
- 🛏️ View bedrooms and bathrooms
- 🅿️ View available facilities
- ❤️ Save favorite properties
- 👤 Property owner profiles
- 🏢 Owner dashboard
- ➕ Owners can post houses and shops
- ✏️ Edit property listings
- 🗑️ Remove listings
- 📱 Mobile-friendly interface
- 🌐 Simple and modern UI
- 🔐 User authentication
- 🗄️ Supabase PostgreSQL database

## 🎯 Problem

Finding rental properties can be difficult because information is often scattered across different platforms, social media posts, brokers, and local contacts.

RentNear aims to provide a simple platform where users can discover rental properties in their preferred area and directly connect with property owners.

## 💡 How RentNear Works

### For Tenants

1. Open RentNear
2. Select your location
3. Search for a property
4. Filter by property type
5. View property details
6. Save interesting properties
7. Contact the owner

### For Property Owners

1. Create an account
2. Open the Owner Hub
3. Add your property
4. Enter price, size, location, facilities, and other details
5. Publish the listing
6. Manage tenant inquiries

## 🏘️ Property Types

RentNear supports different types of properties:

- 🏠 House
- 🏢 Flat
- 🏬 Shop
- 🛏️ Room
- 🏢 Office
- 🏘️ Other rental properties

## 🛠️ Technology

The project is built using modern web technologies.

| Technology | Purpose |
|---|---|
| HTML / CSS | User interface |
| JavaScript | Application logic |
| Supabase | Database & backend services |
| PostgreSQL | Property data storage |
| Responsive Design | Mobile and desktop support |

## 🗄️ Backend

RentNear uses **Supabase** for backend functionality.

The database stores information such as:

- Users
- Properties
- Property owners
- Rental prices
- Locations
- Property facilities
- Saved properties
- Availability status

## 🔒 Security

Sensitive credentials such as Supabase secret keys should never be included in the source code or public GitHub repository.

Use environment variables for private configuration.

Example:

```env
SUPABASE_URL=your_supabase_project_url
SUPABASE_ANON_KEY=your_supabase_publishable_key
