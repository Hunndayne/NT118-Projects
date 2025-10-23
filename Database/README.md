# Sơ đồ Cơ sở dữ liệu (Database Schema)





## Chi tiết các bảng (Data Dictionary)


### 1. users
[cite_start][cite: 1]

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | user_id | id duy nhất cho mỗi người dùng | [cite_start]Khóa chính [cite: 1] |
| [cite_start]2 | email | email | [cite: 1] |
| [cite_start]3 | phone | số điện thoại | [cite: 1] |
| [cite_start]4 | password_hash | mật khẩu đã hash | [cite: 1] |
| [cite_start]5 | full_name | họ và tên | [cite: 1] |
| [cite_start]6 | is_active | tài khoản còn hoạt động hay không | [cite: 1] |
| [cite_start]7 | is_admin | có phải admin (giáo viên) hay không | [cite: 1] |
| [cite_start]8 | created_at | thời điểm tạo tài khoản | [cite: 1] |
| [cite_start]9 | last_login_at | thời điểm lần cuối đăng nhập | [cite: 1] |

### 2. user_profiles
[cite_start][cite: 2]

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | user_id | ID người dùng, liên kết với bảng users | [cite_start]Khóa chính & Khóa ngoại tới users.user_id [cite: 2] |
| 2 | avatar_url | [cite_start]Đường dẫn (URL) đến ảnh đại diện | [cite: 2] |
| 3 | bio | [cite_start]Phần giới thiệu, tiểu sử ngắn về người dùng | [cite: 2] |
| 4 | birthday | [cite_start]Ngày sinh của người dùng | [cite: 2] |
| 5 | updated_at | [cite_start]Thời điểm cập nhật hồ sơ lần cuối | [cite: 2] |

### 3. courses
[cite_start][cite: 3]

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | course_id | ID duy nhất cho mỗi khóa học | [cite_start]Khóa chính [cite: 3] |
| 2 | code | [cite_start]Mã của khóa học (ví dụ: TOEIC_550, IELTS_ADV, IT004) | [cite: 3] |
| 3 | name | [cite_start]Tên đầy đủ của khóa học | [cite: 3] |
| 4 | level | [cite_start]Trình độ của khóa học (ví dụ: Beginner, Intermediate, B1, C2) | [cite: 3] |
| 5 | description | [cite_start]Mô tả chi tiết về nội dung, mục tiêu của khóa học | [cite: 3] |
| 6 | status | [cite_start]Trạng thái khóa học (ví dụ: còn mở, đã đóng) | [cite: 3] |
| 7 | created_by | ID của admin hoặc giáo viên đã tạo khóa học | [cite_start]Khóa ngoại tới users.user_id [cite: 3] |
| 8 | created_at | [cite_start]Thời điểm tạo khóa học | [cite: 3] |

### 4. classes
[cite_start][cite: 4]

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | class_id | ID duy nhất cho mỗi lớp học | [cite_start]Khóa chính [cite: 4] |
| 2 | course_id | ID khóa học mà lớp này thuộc về | [cite_start]Khóa ngoại tới courses.course_id [cite: 4] |
| 3 | class_name | [cite_start]Tên của lớp học (ví dụ: "Đọc nghe - Tối 3-5-7", “Nói viết – Tối 2-4-6”) | [cite: 4] |
| 4 | start_date | [cite_start]Ngày khai giảng lớp học | [cite: 4] |
| 5 | end_date | [cite_start]Ngày kết thúc lớp học | [cite: 4] |
| 6 | is_active | [cite_start]Trạng thái lớp học còn hoạt động hay không | [cite: 4] |
| 7 | description | [cite_start]Mô tả thêm về lớp học (phòng học, lịch học...) | [cite: 4] |
| 8 | created_at | [cite_start]Thời điểm tạo lớp học | [cite: 4] |
| 9 | created_by | Id của người đã tạo lớp | [cite_start]Khóa ngoại tới users.user_id [cite: 4] |

### 5. lessons
[cite_start][cite: 5]

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | lesson_id | ID duy nhất cho mỗi bài học | [cite_start]Khóa chính [cite: 5] |
| 2 | class_id | ID của lớp học chứa bài học này | [cite_start]Khóa ngoại tới classes.class_id [cite: 5] |
| 3 | title | [cite_start]Tiêu đề của bài học (ví dụ: "Unit 1: Tenses") | [cite: 5] |
| 4 | description | [cite_start]Mô tả ngắn về nội dung bài học | [cite: 5] |
| 5 | created_at | [cite_start]Thời điểm tạo bài học | [cite: 5] |
| 6 | created_by | ID của người đã tạo bài học | [cite_start]Khóa ngoại tới users.user_id [cite: 5] |
| 7 | updated_at | [cite_start]Thời điểm cập nhật bài học lần cuối | [cite: 5] |
| 8 | order_index | [cite_start]Số thứ tự của bài học trong lớp học | [cite: 5] |

### 6. lesons_resources
[cite_start][cite: 6]

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | resource_id | ID duy nhất cho mỗi tài nguyên | [cite_start]Khóa chính [cite: 6] |
| 2 | lesson_id | ID của bài học mà tài nguyên này thuộc về | [cite_start]Khóa ngoại tới lessons.lesson_id [cite: 6] |
| 3 | type | Loại tài nguyên | [cite_start]Cần xem lại: Nên gộp res_type và type thành một cột duy nhất. [cite: 6] |
| 4 | title | [cite_start]Tên của tài nguyên | [cite: 6] |
| 5 | content | [cite_start]Nội dung (nếu là text) hoặc mô tả | [cite: 6] |
| 6 | url | [cite_start]Đường dẫn URL (nếu tài nguyên là link) | [cite: 6] |
| 7 | file_path | [cite_start]Đường dẫn tới file tài nguyên (nếu là file upload) | [cite: 6] |
| 8 | created_at | [cite_start]Thời điểm tạo tài nguyên | [cite: 6] |
| 9 | updated_at | [cite_start]Thời điểm cập nhật lần cuối | [cite: 6] |

### 7. assignments
[cite_start][cite: 7]

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | assignment_id | ID duy nhất cho mỗi bài tập | [cite_start]Khóa chính [cite: 7] |
| 2 | class_id | ID của lớp học được giao bài tập này | [cite_start]Khóa ngoại tới classes.class_id [cite: 7] |
| 3 | title | [cite_start]Tiêu đề bài tập | [cite: 7] |
| 4 | description | [cite_start]Mô tả chi tiết yêu cầu bài tập | [cite: 7] |
| 5 | attachment_url | [cite_start]Đường dẫn đến file đính kèm (đề bài) | [cite: 7] |
| 6 | deadline | [cite_start]Hạn chót nộp bài | [cite: 7] |
| 7 | created_by | ID của giáo viên tạo bài tập | [cite_start]Khóa ngoại tới users.user_id [cite: 7] |
| 8 | created_at | [cite_start]Thời điểm tạo bài tập | [cite: 7] |
| 9 | weight | [cite_start]Trọng số điểm của bài tập trong tổng điểm (vd: 20%) | [cite: 7] |

### 8. submissions
[cite_start][cite: 8]

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | submission_id | ID duy nhất cho một lần nộp bài | [cite_start]Khóa chính [cite: 8] |
| 2 | assignment_id | ID của bài tập được nộp | [cite_start]Khóa ngoại tới assignment.assignment_id [cite: 8] |
| 3 | student_id | ID của học viên nộp bài | [cite_start]Khóa ngoại tới users.user_id [cite: 8] |
| 4 | attempt_no | [cite_start]Lần nộp thứ mấy (nếu cho phép nộp lại) | [cite: 8] |
| 5 | submitted_at | [cite_start]Thời điểm học viên nộp bài | [cite: 8] |
| 6 | content | [cite_start]Nội dung bài nộp (dạng văn bản) | [cite: 8] |
| 7 | file_url | [cite_start]Đường dẫn đến file bài làm của học viên | [cite: 8] |
| 8 | status | [cite_start]Trạng thái (đã nộp, trễ hạn, đã chấm...) | [cite: 8] |
| 9 | score | [cite_start]Điểm số của bài nộp | [cite: 8] |
| 10 | feedback | [cite_start]Nhận xét, góp ý của giáo viên | [cite: 8] |
| 11 | grade_by | ID của giáo viên chấm bài | [cite_start]Khóa ngoại tới users.user_id [cite: 8] |
| 12 | grade_at | [cite_start]Thời điểm chấm bài | [cite: 8] |

### 9. announcements_all
[cite_start]*(Ghi chú: bảng thông báo cho mọi người dùng) [cite: 9]*

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | announcement_id | ID duy nhất cho mỗi thông báo | [cite_start]Khóa chính [cite: 9] |
| 2 | title | [cite_start]Tiêu đề của thông báo | [cite: 9] |
| 3 | body | [cite_start]Nội dung chi tiết của thông báo | [cite: 9] |
| 4 | created_by | ID người tạo thông báo | [cite_start]Khóa ngoại tới users.user_id [cite: 9] |
| 5 | created_at | [cite_start]Thời điểm tạo thông báo | [cite: 9] |

### 10. announcements_courses
[cite_start]*(Ghi chú: bảng thông báo cho khóa học cụ thể) [cite: 10]*

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | announcement_id | ID duy nhất cho mỗi thông báo | [cite_start]Khóa chính [cite: 10] |
| 2 | title | [cite_start]Tiêu đề của thông báo | [cite: 10] |
| 3 | body | [cite_start]Nội dung chi tiết của thông báo | [cite: 10] |
| 4 | target_id | ID của khóa học được thông báo | [cite_start]Khóa ngoại tới courses.course_id [cite: 10] |
| 5 | created_by | ID người tạo thông báo | [cite_start]Khóa ngoại tới users.user_id [cite: 10] |
| 6 | created_at | [cite_start]Thời điểm tạo thông báo | [cite: 10] |

### 11. announcements_classes
[cite_start]*(Ghi chú: bảng thông báo cho lớp học cụ thể) [cite: 11]*

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | announcement_id | ID duy nhất cho mỗi thông báo | [cite_start]Khóa chính [cite: 11] |
| 2 | title | [cite_start]Tiêu đề của thông báo | [cite: 11] |
| 3 | body | [cite_start]Nội dung chi tiết của thông báo | [cite: 11] |
| 4 | target_id | ID của khóa học được thông báo | [cite_start]Khóa ngoại tới classes.class_id [cite: 11] |
| 5 | created_by | ID người tạo thông báo | [cite_start]Khóa ngoại tới users.user_id [cite: 11] |
| 6 | created_at | [cite_start]Thời điểm tạo thông báo | [cite: 11] |

### 12. lesson_progess
[cite_start]*(Ghi chú: bảng nhiều nhiều của lessons và users) [cite: 12]*

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | student_id | ID duy nhất của mỗi học viên | [cite_start]Khóa chính, Khóa ngoại tới users.user_id [cite: 12] |
| 2 | lesson_id | ID duy nhất của mỗi bài học | [cite_start]Khóa chính, Khóa ngoại tới lessons.lesson_id [cite: 12] |
| 3 | is_completed | [cite_start]Đã hoàn thành hay chưa | [cite: 12] |
| 4 | last_viewed | [cite_start]Lần cuối xem bài học | [cite: 12] |

### 13. classes_teachers
[cite_start]*(Ghi chú: một giáo viên có thể dạy nhiều lớp, và một lớp có thể được dạy bởi nhiều giáo viên, kết quả của bảng nhiều nhiều) [cite: 13]*

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | class_id | ID duy nhất của mỗi lớp học | [cite_start]Khóa chính, Khóa ngoại tới classes.class_id [cite: 13] |
| 2 | teacher_id | ID duy nhất của mỗi giáo viên | [cite_start]Khóa chính, Khóa ngoại tới users.user_id [cite: 13] |

### 14. courses_students
[cite_start]*(Ghi chú: một học viên có thể học nhiều khóa, và một khóa có thể có nhiều học viên học, kết quả của bảng nhiều nhiều) [cite: 14]*

| STT | Cột | Diễn giải | Ghi chú |
|---|---|---|---|
| 1 | course_id | ID duy nhất của mỗi lớp học | [cite_start]Khóa chính, Khóa ngoại tới courses.course_id [cite: 14] |
| 2 | student_id | ID duy nhất của mỗi học viên | [cite_start]Khóa chính, Khóa ngoại tới users.user_id [cite: 14] |