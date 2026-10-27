INSERT INTO restaurant (name, city, cuisine, seat_count,
                       weekday_opens_at, weekday_closes_at, weekend_opens_at, weekend_closes_at) VALUES
  ('Pelikan', 'Stockholm', 'Svensk', 60, '11:00', '23:00', '12:00', '22:00'),
  ('Sushi Yama', 'Göteborg', 'Japansk', 40, '11:00', '23:00', '12:00', '22:00'),
  ('Trattoria Roma', 'Malmö', 'Italiensk', 50, '11:00', '23:00', '12:00', '22:00');

INSERT INTO booking (restaurant_id, guest_name, guest_email, date_time, party_size, status) VALUES
  (1, 'Anna Svensson', 'anna@example.com', '2026-11-01 18:00:00', 4, 'REQUESTED'),
  (1, 'Erik Lind', 'erik@example.com', '2026-11-02 19:30:00', 2, 'REQUESTED'),
  (2, 'Sara Berg', 'sara@example.com', '2026-11-03 20:00:00', 6, 'REQUESTED');
