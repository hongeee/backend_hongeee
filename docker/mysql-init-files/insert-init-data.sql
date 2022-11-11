INSERT INTO request_vacation.user (id, created_date, modified_date, annual_days, email, name, password)
VALUES (1, '2022-11-11 13:21:00.856000', '2022-11-11 13:21:00.856000', 15, 'hongeee@kakaostyle.com', 'hongeee',
        '{bcrypt}$2a$10$X9mwwfhX4dAsMOuAzlNYBe3q.XFtTVi/bD5DED8pieK38uBvJJxhG');

INSERT INTO request_vacation.user_roles (user_id, roles)
VALUES (1, 'ROLE_USER');