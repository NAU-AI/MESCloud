UPDATE company
SET name = 'M.A. SILVA'
WHERE id = 1;

UPDATE factory
SET name = 'Porto'
WHERE id = 1;

UPDATE users
SET company_id = 1;

UPDATE section
SET name = 'One by one'
WHERE id = 1;

ALTER TABLE section_config
ALTER COLUMN name TYPE VARCHAR(30);

INSERT INTO section_config (section_id, label) VALUES
(1, 'dashboard'),
(1, 'machine-center'),
(1, 'production-management'),
(1, 'alarms');

INSERT INTO feature (name) VALUES
('Gauge'),
('Production Graph'),
('OEE Graph'),
('Production'),
('Alarm');

INSERT INTO section_config_feature (section_config_id, feature_id) VALUES
(1, 1),
(1, 2),
(1, 3),
(2, 1),
(2, 2),
(2, 3),
(3, 4),
(4, 5);

INSERT INTO counting_equipment_feature (counting_equipment_id, feature_id)
VALUES
  (1, 1),
  (1, 2),
  (1, 3),
  (2, 1),
  (2, 2),
  (2, 3),
  (3, 1),
  (3, 2),
  (3, 3),
  (4, 1),
  (4, 2),
  (4, 3);

INSERT INTO audit_script (run_date, process, version, schema)
VALUES
    (CURRENT_DATE, '0002_section_and_feature_config', '1.0.1', '1.0.1_0002');