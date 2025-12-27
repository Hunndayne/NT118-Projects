-- ===== users =====
create table users (
    user_id         bigint generated always as identity primary key,
    email           varchar(255),
    phone           varchar(20),
    password_hash   text,
    full_name       varchar(255),
    is_active       boolean,
    is_admin        boolean,
    created_at      timestamptz,
    last_login_at   timestamptz
);

-- ===== user_profiles =====
create table user_profiles (
    user_id     bigint primary key references users(user_id),
    avatar_url  text,
    bio         text,
    birthday    date,
    updated_at  timestamptz
);

-- ===== courses =====
create table courses (
    course_id   bigint generated always as identity primary key,
    code        varchar(64),
    name        varchar(255),
    level       varchar(64),
    description text,
    status      varchar(32),
    created_by  bigint references users(user_id),
    created_at  timestamptz
);

-- ===== classes =====
create table classes (
    class_id    bigint generated always as identity primary key,
    course_id   bigint references courses(course_id),
    class_name  varchar(255),
    start_date  date,
    end_date    date,
    is_active   boolean,
    description text,
    created_at  timestamptz,
    created_by  bigint references users(user_id)
);

-- ===== lessons =====
create table lessons (
    lesson_id    bigint generated always as identity primary key,
    class_id     bigint references classes(class_id),
    title        varchar(255),
    description  text,
    created_at   timestamptz,
    created_by   bigint references users(user_id),
    updated_at   timestamptz,
    order_index  integer
);

-- ===== lesons_resources (giữ nguyên tên theo tài liệu) =====
create table lesons_resources (
    resource_id  bigint generated always as identity primary key,
    lesson_id    bigint references lessons(lesson_id),
    type         varchar(32),
    title        varchar(255),
    content      text,
    url          text,
    file_path    text,
    created_at   timestamptz,
    updated_at   timestamptz
);

-- ===== assignments =====
create table assignments (
    assignment_id  bigint generated always as identity primary key,
    class_id       bigint references classes(class_id),
    title          varchar(255),
    description    text,
    attachment_url text,
    deadline       timestamptz,
    created_by     bigint references users(user_id),
    created_at     timestamptz,
    weight         numeric(5,2)
);

-- ===== submissions =====
create table submissions (
    submission_id  bigint generated always as identity primary key,
    assignment_id  bigint references assignments(assignment_id),
    student_id     bigint references users(user_id),
    attempt_no     integer,
    submitted_at   timestamptz,
    content        text,
    file_url       text,
    status         varchar(24),
    score          numeric(5,2),
    feedback       text,
    grade_by       bigint references users(user_id),
    grade_at       timestamptz
);

-- ===== password_reset_tokens =====
create table password_reset_tokens (
    token_id    bigint generated always as identity primary key,
    user_id     bigint references users(user_id),
    otp_code    varchar(10) not null,
    expires_at  timestamptz not null,
    used_at     timestamptz,
    created_at  timestamptz not null
);

-- ===== announcements_all =====
create table announcements_all (
    announcement_id  bigint generated always as identity primary key,
    title            varchar(255),
    body             text,
    created_by       bigint references users(user_id),
    created_at       timestamptz
);

-- ===== announcements_courses =====
create table announcements_courses (
    announcement_id  bigint generated always as identity primary key,
    title            varchar(255),
    body             text,
    target_id        bigint references courses(course_id),
    created_by       bigint references users(user_id),
    created_at       timestamptz
);

-- ===== announcements_classes =====
create table announcements_classes (
    announcement_id  bigint generated always as identity primary key,
    title            varchar(255),
    body             text,
    target_id        bigint references classes(class_id),
    created_by       bigint references users(user_id),
    created_at       timestamptz
);

-- ===== notifications =====
create table notifications (
    notification_id  bigint generated always as identity primary key,
    type             varchar(32) not null,
    title            varchar(255) not null,
    content          text,
    created_by       bigint references users(user_id),
    target_user_id   bigint references users(user_id),
    target_class_id  bigint references classes(class_id),
    is_read          boolean not null default false,
    created_at       timestamptz not null
);

-- ===== lesson_progess (giữ nguyên tên theo tài liệu) =====
create table lesson_progess (
    student_id    bigint references users(user_id),
    lesson_id     bigint references lessons(lesson_id),
    is_completed  boolean,
    last_viewed   timestamptz,
    primary key (student_id, lesson_id)
);

-- ===== classes_teachers =====
create table classes_teachers (
    class_id    bigint references classes(class_id),
    teacher_id  bigint references users(user_id),
    primary key (class_id, teacher_id)
);

-- ===== courses_students =====
create table courses_students (
    course_id   bigint references courses(course_id),
    student_id  bigint references users(user_id),
    primary key (course_id, student_id)
);

-- ===== courses_teachers =====
create table courses_teachers (
    course_id   bigint references courses(course_id),
    teacher_id  bigint references users(user_id),
    primary key (course_id, teacher_id)
);
