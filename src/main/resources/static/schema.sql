-- 1. ตารางสัตว์เลี้ยง
create table pets (
    id bigserial primary key,
    user_id bigint references users(id) on delete cascade,
    name text not null,
    type text,
    breed text,
    sex text,
    age int,
    weight numeric,
    about_pet text,
    image_url text,
    created_at timestamptz default now()
);
-- 2. ตารางประเภท services
create table services (
    id bigserial primary key,
    name text not null,
    description text
);
-- 3. ตาราง services ที่ Sitter แต่ละคนเปิดรับ (เก็บราคาที่นี่)
create table sitter_services (
    id bigserial primary key,
    sitter_id bigint references users(id) on delete cascade,
    service_id bigint references services(id) on delete cascade,
    price_per_hour numeric not null,
    is_available boolean default true
);
-- 4. ตารางการจอง (ต่อไปที่ sitter_services เลย)
create table bookings (
    id bigserial primary key,
    user_id bigint references users(id),
    -- owner
    sitter_id bigint references users(id),
    -- sitter
    pet_id bigint references pets(id),
    -- pet
    sitter_service_id bigint references sitter_services(id),
    -- เจาะจง services และราคาของ Sitter คนนั้น
    start_date date not null,
    end_date date not null,
    start_time time,
    end_time time,
    status text check (
        status in ('PENDING', 'VERIFIED', 'SUCCESS', 'CANCELLED')
    ) default 'PENDING',
    total_price numeric,
    note_to_sitter text,
    created_at timestamptz default now()
);

-- 1. เชื่อม Payments กลับไปหา Bookings
alter table payments
add constraint payments_booking_id_fkey foreign key (booking_id) references bookings(id) on delete cascade;
-- 2. เชื่อม Messages กลับไปหา Bookings
alter table messages
add constraint messages_booking_id_fkey foreign key (booking_id) references bookings(id) on delete cascade;
-- 3. เชื่อม Reviews กลับไปหา Bookings
alter table reviews
add constraint reviews_booking_id_fkey foreign key (booking_id) references bookings(id) on delete cascade;
-- 4. เชื่อม Notifications กลับไปหา Bookings (ยังไม่ได้สร้าง หากจะใช้ในอนาคตให้ รันคำสั่งนี้)
alter table notifications
add constraint notifications_booking_id_fkey foreign key (booking_id) references bookings(id) on delete cascade;

-- 5. เพิ่มข้อมูลบริการเริ่มต้น
INSERT INTO public.services (name, description)
VALUES 
  ('Pet Boarding', 'บริการรับฝากเลี้ยงค้างคืน ดูแลตลอด 24 ชั่วโมง'),
  ('Dog Walking', 'บริการพาสุนัขเดินเล่น ออกกำลังกาย 30-60 นาที'),
  ('Pet Grooming', 'บริการอาบน้ำ ตัดขน และทำสปาสัตว์เลี้ยง'),
  ('Daycare', 'บริการรับฝากเลี้ยงระหว่างวัน สำหรับเจ้าของที่ไม่ว่าง');