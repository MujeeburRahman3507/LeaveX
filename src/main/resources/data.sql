---------------------------------------------------
-- SEED USERS
-- Password for all users = Admin@123
---------------------------------------------------

INSERT INTO users
(id, employee_code, full_name, email, password, role, manager_id)
VALUES
(
1,
'ADM001',
'System Administrator',
'admin@boitglobal.com',
'$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFXm6Zx0QKq0J6M3P7Jrped1IovnHg56',
'ADMIN',
NULL
);

INSERT INTO users
(id, employee_code, full_name, email, password, role, manager_id)
VALUES
(
2,
'MGR001',
'Prasanna Manager',
'manager@boitglobal.com',
'$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFXm6Zx0QKq0J6M3P7Jrped1IovnHg56',
'MANAGER',
NULL
);

INSERT INTO users
(id, employee_code, full_name, email, password, role, manager_id)
VALUES
(
3,
'EMP001',
'Mujeebur Rahman',
'mujeeb@boitglobal.com',
'$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFXm6Zx0QKq0J6M3P7Jrped1IovnHg56',
'EMPLOYEE',
2
);

INSERT INTO users
(id, employee_code, full_name, email, password, role, manager_id)
VALUES
(
4,
'EMP002',
'John Employee',
'john@boitglobal.com',
'$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFXm6Zx0QKq0J6M3P7Jrped1IovnHg56',
'EMPLOYEE',
2
);

INSERT INTO leave_balance
(user_id, annual_balance, used_leave, remaining_leave)
VALUES
(1,24,0,24),
(2,24,0,24),
(3,24,0,24),
(4,24,0,24);