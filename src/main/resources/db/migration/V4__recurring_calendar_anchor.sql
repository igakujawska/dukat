-- Zachowujemy dzień miesiąca: 31 stycznia -> ostatni dzień lutego -> 31 marca.
ALTER TABLE recurring_payments ADD COLUMN anchor_day INTEGER NOT NULL DEFAULT 1;
UPDATE recurring_payments SET anchor_day=EXTRACT(DAY FROM next_due_date)::INTEGER;
