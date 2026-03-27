-- ============================================================
-- Pet Sitter App - Database Migration
-- Run this in Supabase SQL Editor
-- ============================================================

-- 1. DROP old redundant table
DROP TABLE IF EXISTS public.pet_sitters;

-- 2. MODIFY bookings.status values
ALTER TABLE public.bookings
  DROP CONSTRAINT IF EXISTS bookings_status_check;
ALTER TABLE public.bookings
  ADD CONSTRAINT bookings_status_check
  CHECK (status = ANY (ARRAY[
    'WAITING_FOR_CONFIRM',
    'WAITING_FOR_SERVICE',
    'IN_SERVICE',
    'SUCCESS',
    'CANCELLED'
  ]));
ALTER TABLE public.bookings ALTER COLUMN status SET DEFAULT 'WAITING_FOR_CONFIRM';

-- Remove single pet_id column (replaced by booking_pets)
ALTER TABLE public.bookings DROP COLUMN IF EXISTS pet_id;

-- 3. MODIFY sitter_profiles
ALTER TABLE public.sitter_profiles DROP COLUMN IF EXISTS is_approved;
ALTER TABLE public.sitter_profiles
  ADD COLUMN IF NOT EXISTS trade_name       text,
  ADD COLUMN IF NOT EXISTS pet_types        text,
  ADD COLUMN IF NOT EXISTS status           text NOT NULL DEFAULT 'WAITING_FOR_APPROVE'
                                            CHECK (status = ANY (ARRAY['WAITING_FOR_APPROVE','APPROVED','REJECTED'])),
  ADD COLUMN IF NOT EXISTS reject_reason    text,
  ADD COLUMN IF NOT EXISTS phone            text,
  ADD COLUMN IF NOT EXISTS id_number        text,
  ADD COLUMN IF NOT EXISTS date_of_birth    date,
  ADD COLUMN IF NOT EXISTS place_description text,
  ADD COLUMN IF NOT EXISTS address_id       bigint REFERENCES public.addresses(id);

-- 4. MODIFY user_profiles
ALTER TABLE public.user_profiles
  ADD COLUMN IF NOT EXISTS id_number     text,
  ADD COLUMN IF NOT EXISTS date_of_birth date;

-- 5. MODIFY pets
ALTER TABLE public.pets
  ADD COLUMN IF NOT EXISTS color text;

-- 6. MODIFY messages
ALTER TABLE public.messages
  ADD COLUMN IF NOT EXISTS receiver_id bigint REFERENCES public.users(id),
  ADD COLUMN IF NOT EXISTS image_url   text;
ALTER TABLE public.messages ALTER COLUMN booking_id DROP NOT NULL;

-- 7. MODIFY payments
ALTER TABLE public.payments
  ADD COLUMN IF NOT EXISTS transaction_no   text,
  ADD COLUMN IF NOT EXISTS transaction_date date,
  ADD COLUMN IF NOT EXISTS slip_image_url   text;

-- 8. ADD booking_pets junction table
CREATE TABLE IF NOT EXISTS public.booking_pets (
  id         bigint NOT NULL DEFAULT nextval('booking_pets_id_seq'::regclass),
  booking_id bigint NOT NULL,
  pet_id     bigint NOT NULL,
  CONSTRAINT booking_pets_pkey PRIMARY KEY (id),
  CONSTRAINT booking_pets_booking_id_fkey FOREIGN KEY (booking_id) REFERENCES public.bookings(id),
  CONSTRAINT booking_pets_pet_id_fkey    FOREIGN KEY (pet_id)     REFERENCES public.pets(id)
);
CREATE SEQUENCE IF NOT EXISTS public.booking_pets_id_seq;

-- 9. ADD reports table
CREATE TABLE IF NOT EXISTS public.reports (
  id                  bigint NOT NULL DEFAULT nextval('reports_id_seq'::regclass),
  reporter_id         bigint,
  reported_sitter_id  bigint,
  issue               text,
  description         text,
  status              text NOT NULL DEFAULT 'NEW'
                      CHECK (status = ANY (ARRAY['NEW','PENDING','RESOLVED','CANCELLED'])),
  created_at          timestamp with time zone DEFAULT now(),
  CONSTRAINT reports_pkey PRIMARY KEY (id),
  CONSTRAINT reports_reporter_id_fkey        FOREIGN KEY (reporter_id)        REFERENCES public.users(id),
  CONSTRAINT reports_reported_sitter_id_fkey FOREIGN KEY (reported_sitter_id) REFERENCES public.users(id)
);
CREATE SEQUENCE IF NOT EXISTS public.reports_id_seq;

-- 10. ADD payout_options table
CREATE TABLE IF NOT EXISTS public.payout_options (
  id                  bigint NOT NULL DEFAULT nextval('payout_options_id_seq'::regclass),
  sitter_id           bigint UNIQUE,
  bank_name           text,
  account_number      text,
  account_name        text,
  book_bank_image_url text,
  CONSTRAINT payout_options_pkey      PRIMARY KEY (id),
  CONSTRAINT payout_options_sitter_id_fkey FOREIGN KEY (sitter_id) REFERENCES public.users(id)
);
CREATE SEQUENCE IF NOT EXISTS public.payout_options_id_seq;

-- 11. ADD sitter_images table
CREATE TABLE IF NOT EXISTS public.sitter_images (
  id        bigint NOT NULL DEFAULT nextval('sitter_images_id_seq'::regclass),
  sitter_id bigint,
  image_url text,
  CONSTRAINT sitter_images_pkey      PRIMARY KEY (id),
  CONSTRAINT sitter_images_sitter_id_fkey FOREIGN KEY (sitter_id) REFERENCES public.sitter_profiles(id)
);
CREATE SEQUENCE IF NOT EXISTS public.sitter_images_id_seq;
