 --
-- PostgreSQL database dump
--

\restrict hfPvYFugUnT5AjnVTyjryQnFtOpvBFycGTtaS6AOBeGNNeuTNerrbwTmP04GZl0

-- Dumped from database version 16.10 (Ubuntu 16.10-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.10 (Ubuntu 16.10-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: citext; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;


--
-- Name: EXTENSION citext; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION citext IS 'data type for case-insensitive character strings';


--
-- Name: token_type; Type: TYPE; Schema: public; Owner: hunn
--

CREATE TYPE public.token_type AS ENUM (
    'access',
    'refresh',
    'personal'
);


ALTER TYPE public.token_type OWNER TO hunn;

--
-- Name: tokentype; Type: TYPE; Schema: public; Owner: hunn
--

CREATE TYPE public.tokentype AS ENUM (
    'ACCESS',
    'PERSONAL',
    'REFRESH'
);


ALTER TYPE public.tokentype OWNER TO hunn;

--
-- Name: CAST (public.tokentype AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.tokentype AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.tokentype); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.tokentype) WITH INOUT AS IMPLICIT;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: announcements_all; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.announcements_all (
    announcement_id bigint NOT NULL,
    title character varying(255),
    body text,
    created_by bigint,
    created_at timestamp with time zone
);


ALTER TABLE public.announcements_all OWNER TO hunn;

--
-- Name: announcements_all_announcement_id_seq; Type: SEQUENCE; Schema: public; Owner: hunn
--

ALTER TABLE public.announcements_all ALTER COLUMN announcement_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.announcements_all_announcement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: announcements_classes; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.announcements_classes (
    announcement_id bigint NOT NULL,
    title character varying(255),
    body text,
    target_id bigint,
    created_by bigint,
    created_at timestamp with time zone
);


ALTER TABLE public.announcements_classes OWNER TO hunn;

--
-- Name: announcements_classes_announcement_id_seq; Type: SEQUENCE; Schema: public; Owner: hunn
--

ALTER TABLE public.announcements_classes ALTER COLUMN announcement_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.announcements_classes_announcement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: announcements_courses; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.announcements_courses (
    announcement_id bigint NOT NULL,
    title character varying(255),
    body text,
    target_id bigint,
    created_by bigint,
    created_at timestamp with time zone
);


ALTER TABLE public.announcements_courses OWNER TO hunn;

--
-- Name: announcements_courses_announcement_id_seq; Type: SEQUENCE; Schema: public; Owner: hunn
--

ALTER TABLE public.announcements_courses ALTER COLUMN announcement_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.announcements_courses_announcement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: assignments; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.assignments (
    assignment_id bigint NOT NULL,
    class_id bigint,
    title character varying(255),
    description text,
    attachment_url text,
    deadline timestamp with time zone,
    created_by bigint,
    created_at timestamp with time zone,
    weight numeric(5,2)
);


ALTER TABLE public.assignments OWNER TO hunn;

--
-- Name: assignments_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: hunn
--

ALTER TABLE public.assignments ALTER COLUMN assignment_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.assignments_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: auth_tokens; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.auth_tokens (
    token_id bigint NOT NULL,
    user_id bigint,
    token_jti uuid NOT NULL,
    token_hash character varying(255) NOT NULL,
    type character varying(255) DEFAULT 'access'::public.token_type NOT NULL,
    revoked boolean DEFAULT false NOT NULL,
    issued_at timestamp with time zone DEFAULT now() NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    user_agent character varying(255),
    ip_address inet
);


ALTER TABLE public.auth_tokens OWNER TO hunn;

--
-- Name: auth_tokens_token_id_seq; Type: SEQUENCE; Schema: public; Owner: hunn
--

ALTER TABLE public.auth_tokens ALTER COLUMN token_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.auth_tokens_token_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: classes; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.classes (
    class_id bigint NOT NULL,
    course_id bigint,
    class_name character varying(255),
    start_date date,
    end_date date,
    is_active boolean,
    description text,
    created_at timestamp with time zone,
    created_by bigint
);


ALTER TABLE public.classes OWNER TO hunn;

--
-- Name: classes_class_id_seq; Type: SEQUENCE; Schema: public; Owner: hunn
--

ALTER TABLE public.classes ALTER COLUMN class_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.classes_class_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: classes_teachers; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.classes_teachers (
    class_id bigint NOT NULL,
    teacher_id bigint NOT NULL
);


ALTER TABLE public.classes_teachers OWNER TO hunn;

--
-- Name: courses; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.courses (
    course_id bigint NOT NULL,
    code character varying(64),
    name character varying(255),
    level character varying(64),
    description text,
    status character varying(32),
    created_by bigint,
    created_at timestamp with time zone
);


ALTER TABLE public.courses OWNER TO hunn;

--
-- Name: courses_course_id_seq; Type: SEQUENCE; Schema: public; Owner: hunn
--

ALTER TABLE public.courses ALTER COLUMN course_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.courses_course_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: courses_students; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.courses_students (
    course_id bigint NOT NULL,
    student_id bigint NOT NULL
);


ALTER TABLE public.courses_students OWNER TO hunn;

--
-- Name: lesons_resources; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.lesons_resources (
    resource_id bigint NOT NULL,
    lesson_id bigint,
    type character varying(32),
    title character varying(255),
    content text,
    url text,
    file_path text,
    created_at timestamp with time zone,
    updated_at timestamp with time zone
);


ALTER TABLE public.lesons_resources OWNER TO hunn;

--
-- Name: lesons_resources_resource_id_seq; Type: SEQUENCE; Schema: public; Owner: hunn
--

ALTER TABLE public.lesons_resources ALTER COLUMN resource_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.lesons_resources_resource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: lesson_progess; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.lesson_progess (
    student_id bigint NOT NULL,
    lesson_id bigint NOT NULL,
    is_completed boolean,
    last_viewed timestamp with time zone
);


ALTER TABLE public.lesson_progess OWNER TO hunn;

--
-- Name: lessons; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.lessons (
    lesson_id bigint NOT NULL,
    class_id bigint,
    title character varying(255),
    description text,
    created_at timestamp with time zone,
    created_by bigint,
    updated_at timestamp with time zone,
    order_index integer
);


ALTER TABLE public.lessons OWNER TO hunn;

--
-- Name: lessons_lesson_id_seq; Type: SEQUENCE; Schema: public; Owner: hunn
--

ALTER TABLE public.lessons ALTER COLUMN lesson_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.lessons_lesson_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: submissions; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.submissions (
    submission_id bigint NOT NULL,
    assignment_id bigint,
    student_id bigint,
    attempt_no integer,
    submitted_at timestamp with time zone,
    content text,
    file_url text,
    status character varying(24),
    score numeric(5,2),
    feedback text,
    grade_by bigint,
    grade_at timestamp with time zone
);


ALTER TABLE public.submissions OWNER TO hunn;

--
-- Name: submissions_submission_id_seq; Type: SEQUENCE; Schema: public; Owner: hunn
--

ALTER TABLE public.submissions ALTER COLUMN submission_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.submissions_submission_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: user_profiles; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.user_profiles (
    user_id bigint NOT NULL,
    avatar_url character varying(255),
    bio character varying(255),
    birthday date,
    updated_at timestamp with time zone
);


ALTER TABLE public.user_profiles OWNER TO hunn;

--
-- Name: user_tokens; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.user_tokens (
    id character varying(255) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    token character varying(150) NOT NULL,
    user_id character varying(255) NOT NULL
);


ALTER TABLE public.user_tokens OWNER TO hunn;

--
-- Name: users; Type: TABLE; Schema: public; Owner: hunn
--

CREATE TABLE public.users (
    user_id bigint NOT NULL,
    email character varying(255),
    phone character varying(20),
    password_hash character varying(255),
    full_name character varying(255),
    is_active boolean DEFAULT true NOT NULL,
    is_admin boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone,
    last_login_at timestamp with time zone,
    id character varying(255) NOT NULL,
    city character varying(255),
    country character varying(255),
    description character varying(1000),
    email_address character varying(255),
    email_visibility character varying(255),
    firstname character varying(255),
    interest character varying(255),
    lastname character varying(255),
    password character varying(255),
    phone_number character varying(255),
    profile_image_path character varying(255),
    timezone character varying(255),
    username character varying(255),
    first_name character varying(255),
    last_name character varying(255)
);


ALTER TABLE public.users OWNER TO hunn;

--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: hunn
--

ALTER TABLE public.users ALTER COLUMN user_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: announcements_all; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.announcements_all (announcement_id, title, body, created_by, created_at) FROM stdin;
\.


--
-- Data for Name: announcements_classes; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.announcements_classes (announcement_id, title, body, target_id, created_by, created_at) FROM stdin;
\.


--
-- Data for Name: announcements_courses; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.announcements_courses (announcement_id, title, body, target_id, created_by, created_at) FROM stdin;
\.


--
-- Data for Name: assignments; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.assignments (assignment_id, class_id, title, description, attachment_url, deadline, created_by, created_at, weight) FROM stdin;
\.


--
-- Data for Name: auth_tokens; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.auth_tokens (token_id, user_id, token_jti, token_hash, type, revoked, issued_at, expires_at, user_agent, ip_address) FROM stdin;
2       8       51a1a774-f508-48ab-a9a9-374ea9d785e0    cfa101bc855af241888a2169d5bf9428c2d1c4aa6f48b7231165b80992fdb3f0ACCESS  f       2025-10-25 15:05:19.214817+00   2025-10-26 15:05:19.214817+00   \N      \N
\.


--
-- Data for Name: classes; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.classes (class_id, course_id, class_name, start_date, end_date, is_active, description, created_at, created_by) FROM stdin;
\.


--
-- Data for Name: classes_teachers; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.classes_teachers (class_id, teacher_id) FROM stdin;
\.


--
-- Data for Name: courses; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.courses (course_id, code, name, level, description, status, created_by, created_at) FROM stdin;
\.


--
-- Data for Name: courses_students; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.courses_students (course_id, student_id) FROM stdin;
\.


--
-- Data for Name: lesons_resources; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.lesons_resources (resource_id, lesson_id, type, title, content, url, file_path, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: lesson_progess; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.lesson_progess (student_id, lesson_id, is_completed, last_viewed) FROM stdin;
\.


--
-- Data for Name: lessons; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.lessons (lesson_id, class_id, title, description, created_at, created_by, updated_at, order_index) FROM stdin;
\.


--
-- Data for Name: submissions; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.submissions (submission_id, assignment_id, student_id, attempt_no, submitted_at, content, file_url, status, score, feedback, grade_by, grade_at) FROM stdin;
\.


--
-- Data for Name: user_profiles; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.user_profiles (user_id, avatar_url, bio, birthday, updated_at) FROM stdin;
\.


--
-- Data for Name: user_tokens; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.user_tokens (id, created_at, expires_at, token, user_id) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: hunn
--

COPY public.users (user_id, email, phone, password_hash, full_name, is_active, is_admin, created_at, last_login_at, id, city, country, description, email_address, email_visibility, firstname, interest, lastname, password, phone_number, profile_image_path, timezone, username, first_name, last_name) FROM stdin;
8       xxxxxx@gmail.com     +84xxxxxxxx    $2a$10$96dlFJ7g8aFKSMn60X5KPejDXd2H8lAlApGHP7fRxZV40wFT8tczC   Hung Tran Thanh  t       f       2025-10-25 15:05:08.878614+00   2025-10-25 15:05:19.214817+00   8       \N      \N     \N       tranthanhhung1641@gmail.com     \N      \N      \N      \N      \N      +84914889218    \N      \N      hunn   Hung     Tran Thanh
\.


--
-- Name: announcements_all_announcement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hunn
--

SELECT pg_catalog.setval('public.announcements_all_announcement_id_seq', 1, false);


--
-- Name: announcements_classes_announcement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hunn
--

SELECT pg_catalog.setval('public.announcements_classes_announcement_id_seq', 1, false);


--
-- Name: announcements_courses_announcement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hunn
--

SELECT pg_catalog.setval('public.announcements_courses_announcement_id_seq', 1, false);


--
-- Name: assignments_assignment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hunn
--

SELECT pg_catalog.setval('public.assignments_assignment_id_seq', 1, false);


--
-- Name: auth_tokens_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hunn
--

SELECT pg_catalog.setval('public.auth_tokens_token_id_seq', 2, true);


--
-- Name: classes_class_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hunn
--

SELECT pg_catalog.setval('public.classes_class_id_seq', 1, false);


--
-- Name: courses_course_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hunn
--

SELECT pg_catalog.setval('public.courses_course_id_seq', 1, false);


--
-- Name: lesons_resources_resource_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hunn
--

SELECT pg_catalog.setval('public.lesons_resources_resource_id_seq', 1, false);


--
-- Name: lessons_lesson_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hunn
--

SELECT pg_catalog.setval('public.lessons_lesson_id_seq', 1, false);


--
-- Name: submissions_submission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hunn
--

SELECT pg_catalog.setval('public.submissions_submission_id_seq', 1, false);


--
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hunn
--

SELECT pg_catalog.setval('public.users_user_id_seq', 8, true);


--
-- Name: announcements_all announcements_all_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.announcements_all
    ADD CONSTRAINT announcements_all_pkey PRIMARY KEY (announcement_id);


--
-- Name: announcements_classes announcements_classes_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.announcements_classes
    ADD CONSTRAINT announcements_classes_pkey PRIMARY KEY (announcement_id);


--
-- Name: announcements_courses announcements_courses_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.announcements_courses
    ADD CONSTRAINT announcements_courses_pkey PRIMARY KEY (announcement_id);


--
-- Name: assignments assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_pkey PRIMARY KEY (assignment_id);


--
-- Name: auth_tokens auth_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.auth_tokens
    ADD CONSTRAINT auth_tokens_pkey PRIMARY KEY (token_id);


--
-- Name: auth_tokens auth_tokens_token_hash_key; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.auth_tokens
    ADD CONSTRAINT auth_tokens_token_hash_key UNIQUE (token_hash);


--
-- Name: auth_tokens auth_tokens_token_jti_key; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.auth_tokens
    ADD CONSTRAINT auth_tokens_token_jti_key UNIQUE (token_jti);


--
-- Name: classes classes_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.classes
    ADD CONSTRAINT classes_pkey PRIMARY KEY (class_id);


--
-- Name: classes_teachers classes_teachers_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.classes_teachers
    ADD CONSTRAINT classes_teachers_pkey PRIMARY KEY (class_id, teacher_id);


--
-- Name: courses courses_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_pkey PRIMARY KEY (course_id);


--
-- Name: courses_students courses_students_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.courses_students
    ADD CONSTRAINT courses_students_pkey PRIMARY KEY (course_id, student_id);


--
-- Name: lesons_resources lesons_resources_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.lesons_resources
    ADD CONSTRAINT lesons_resources_pkey PRIMARY KEY (resource_id);


--
-- Name: lesson_progess lesson_progess_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.lesson_progess
    ADD CONSTRAINT lesson_progess_pkey PRIMARY KEY (student_id, lesson_id);


--
-- Name: lessons lessons_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.lessons
    ADD CONSTRAINT lessons_pkey PRIMARY KEY (lesson_id);


--
-- Name: submissions submissions_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.submissions
    ADD CONSTRAINT submissions_pkey PRIMARY KEY (submission_id);


--
-- Name: users uk_users_email_address; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_users_email_address UNIQUE (email_address);


--
-- Name: users uk_users_username; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_users_username UNIQUE (username);


--
-- Name: user_tokens ukfvl6k04x11pern525noiw5k6v; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.user_tokens
    ADD CONSTRAINT ukfvl6k04x11pern525noiw5k6v UNIQUE (token);


--
-- Name: user_profiles user_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT user_profiles_pkey PRIMARY KEY (user_id);


--
-- Name: user_tokens user_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.user_tokens
    ADD CONSTRAINT user_tokens_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: idx_auth_tokens_expires; Type: INDEX; Schema: public; Owner: hunn
--

CREATE INDEX idx_auth_tokens_expires ON public.auth_tokens USING btree (expires_at);


--
-- Name: idx_auth_tokens_revoked; Type: INDEX; Schema: public; Owner: hunn
--

CREATE INDEX idx_auth_tokens_revoked ON public.auth_tokens USING btree (revoked);


--
-- Name: idx_auth_tokens_user_id; Type: INDEX; Schema: public; Owner: hunn
--

CREATE INDEX idx_auth_tokens_user_id ON public.auth_tokens USING btree (user_id);


--
-- Name: idx_user_tokens_user_id; Type: INDEX; Schema: public; Owner: hunn
--

CREATE INDEX idx_user_tokens_user_id ON public.user_tokens USING btree (user_id);


--
-- Name: announcements_all announcements_all_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.announcements_all
    ADD CONSTRAINT announcements_all_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(user_id);


--
-- Name: announcements_classes announcements_classes_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.announcements_classes
    ADD CONSTRAINT announcements_classes_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(user_id);


--
-- Name: announcements_classes announcements_classes_target_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.announcements_classes
    ADD CONSTRAINT announcements_classes_target_id_fkey FOREIGN KEY (target_id) REFERENCES public.classes(class_id);


--
-- Name: announcements_courses announcements_courses_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.announcements_courses
    ADD CONSTRAINT announcements_courses_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(user_id);


--
-- Name: announcements_courses announcements_courses_target_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.announcements_courses
    ADD CONSTRAINT announcements_courses_target_id_fkey FOREIGN KEY (target_id) REFERENCES public.courses(course_id);


--
-- Name: assignments assignments_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.classes(class_id);


--
-- Name: assignments assignments_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(user_id);


--
-- Name: auth_tokens auth_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.auth_tokens
    ADD CONSTRAINT auth_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: classes classes_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.classes
    ADD CONSTRAINT classes_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.courses(course_id);


--
-- Name: classes classes_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.classes
    ADD CONSTRAINT classes_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(user_id);


--
-- Name: classes_teachers classes_teachers_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.classes_teachers
    ADD CONSTRAINT classes_teachers_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.classes(class_id);


--
-- Name: classes_teachers classes_teachers_teacher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.classes_teachers
    ADD CONSTRAINT classes_teachers_teacher_id_fkey FOREIGN KEY (teacher_id) REFERENCES public.users(user_id);


--
-- Name: courses courses_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(user_id);


--
-- Name: courses_students courses_students_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.courses_students
    ADD CONSTRAINT courses_students_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.courses(course_id);


--
-- Name: courses_students courses_students_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.courses_students
    ADD CONSTRAINT courses_students_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.users(user_id);


--
-- Name: lesons_resources lesons_resources_lesson_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.lesons_resources
    ADD CONSTRAINT lesons_resources_lesson_id_fkey FOREIGN KEY (lesson_id) REFERENCES public.lessons(lesson_id);


--
-- Name: lesson_progess lesson_progess_lesson_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.lesson_progess
    ADD CONSTRAINT lesson_progess_lesson_id_fkey FOREIGN KEY (lesson_id) REFERENCES public.lessons(lesson_id);


--
-- Name: lesson_progess lesson_progess_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.lesson_progess
    ADD CONSTRAINT lesson_progess_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.users(user_id);


--
-- Name: lessons lessons_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.lessons
    ADD CONSTRAINT lessons_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.classes(class_id);


--
-- Name: lessons lessons_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.lessons
    ADD CONSTRAINT lessons_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(user_id);


--
-- Name: submissions submissions_assignment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.submissions
    ADD CONSTRAINT submissions_assignment_id_fkey FOREIGN KEY (assignment_id) REFERENCES public.assignments(assignment_id);


--
-- Name: submissions submissions_grade_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.submissions
    ADD CONSTRAINT submissions_grade_by_fkey FOREIGN KEY (grade_by) REFERENCES public.users(user_id);


--
-- Name: submissions submissions_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.submissions
    ADD CONSTRAINT submissions_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.users(user_id);


--
-- Name: user_profiles user_profiles_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hunn
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT user_profiles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- PostgreSQL database dump complete
--

\unrestrict hfPvYFugUnT5AjnVTyjryQnFtOpvBFycGTtaS6AOBeGNNeuTNerrbwTmP04GZl0