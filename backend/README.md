# Backend API

Ứng dụng Spring Boot (context-path `/api`). Các endpoint chính:

| URL | Method | Quyền | Mô tả | Request | Response |
| --- | --- | --- | --- | --- | --- |
| `/auth/login` | POST | Public | Đăng nhập, lấy access token | Body `{"username": "...", "password": "..."}` | 200: `{"token","tokenType","expiresAt","admin","role"}` |
| `/auth/logout` | POST | Đã đăng nhập | Đăng xuất, revoke token hiện tại | Header `X-Auth-Token` | 204 No Content |
| `/checklogin` | GET | Đã đăng nhập | Kiểm tra token còn hạn | Header `X-Auth-Token` | 200: `{"valid":true,"expiresAt":"..."}` |
| `/users` | POST | Public | Đăng ký người dùng mới (mặc định role STUDENT) | Body `{"username","password","emailAddress","firstName","lastName","phoneNumber", optional: emailVisibility, city, country, timezone, description, interest, avatarUrl}` | 200: `UserResponse` |
| `/users` | GET | Đã đăng nhập | Lấy thông tin user hiện tại | Header `X-Auth-Token` | 200: `UserResponse` (current user) |
| `/users` | PUT | Đã đăng nhập (chỉ SUPER_ADMIN đổi role) | Cập nhật thông tin user hiện tại | Header `X-Auth-Token`; Body (optional) `{"emailAddress","firstName","lastName","emailVisibility","city","country","timezone","description","interest","phoneNumber","avatarUrl","password","role"}` | 200: `UserResponse` |
| `/users/{id}` | GET | SUPER_ADMIN | Lấy thông tin user theo id | Header `X-Auth-Token` | 200: `UserResponse` |
| `/users/{id}` | PUT | SUPER_ADMIN hoặc chính user | Cập nhật user theo id | Header `X-Auth-Token`; Body như `/users` PUT | 200: `UserResponse` |
| `/courses` | GET | Đã đăng nhập | Danh sách khoá học (SUPER_ADMIN thấy tất cả, student/teacher thấy khoá tham gia) | Header `X-Auth-Token` | 200: `CourseResponse[]` |
| `/courses/{id}` | GET | Đã đăng nhập, có quyền xem | Chi tiết khoá học | Header `X-Auth-Token` | 200: `CourseResponse` |
| `/courses/{id}/participants` | GET | Đã đăng nhập, có quyền xem | Danh sách học viên của khoá | Header `X-Auth-Token` | 200: `CourseParticipantResponse[]` |
| `/courses/{id}/eligible-participants` | GET | SUPER_ADMIN | Danh sách user active chưa tham gia khoá | Header `X-Auth-Token` | 200: danh sách user đủ điều kiện |
| `/courses` | POST | SUPER_ADMIN | Tạo khoá học | Header `X-Auth-Token`; Body `{"code","name", optional: "level","description","active"}` | 200: `CourseResponse` |
| `/courses/{id}` | PUT | SUPER_ADMIN | Cập nhật khoá học | Header `X-Auth-Token`; Body (optional) `{"code","name","level","description","active"}` | 200: `CourseResponse` |
| `/courses/{id}` | DELETE | SUPER_ADMIN | Xoá khoá học | Header `X-Auth-Token` | 200 empty |
| `/courses/{id}/participants` | POST | SUPER_ADMIN | Thêm học viên vào khoá | Header `X-Auth-Token`; Body `{"userIds":[...long...]}` | 200: `CourseResponse` (sau khi thêm) |
| `/courses/{id}/participants` | DELETE | SUPER_ADMIN | Xoá học viên khỏi khoá | Header `X-Auth-Token`; Body `{"userIds":[...long...]}` | 200: `CourseResponse` (sau khi xoá) |
| `/classes` | GET | Đã đăng nhập (SUPER_ADMIN tất cả; TEACHER lớp được phân công) | Danh sách lớp | Header `X-Auth-Token` | 200: `ClassResponse[]` |
| `/classes/{id}` | GET | SUPER_ADMIN hoặc TEACHER của lớp | Chi tiết lớp | Header `X-Auth-Token` | 200: `ClassResponse` |
| `/classes` | POST | SUPER_ADMIN | Tạo lớp mới trong khoá | Header `X-Auth-Token`; Body `{"courseId","name", optional: "description","startDate","endDate","active"}` | 200: `ClassResponse` |
| `/classes/{id}` | PUT | SUPER_ADMIN | Cập nhật lớp | Header `X-Auth-Token`; Body optional `{"name","description","startDate","endDate","active"}` | 200: `ClassResponse` |
| `/classes/{id}` | DELETE | SUPER_ADMIN | Xoá lớp | Header `X-Auth-Token` | 200 empty |
| `/classes/{id}/teachers` | POST | SUPER_ADMIN | Thêm giáo viên vào lớp | Header `X-Auth-Token`; Body `{"teacherIds":[...long...]}` | 200: `ClassResponse` (sau khi thêm) |
| `/classes/{id}/teachers` | DELETE | SUPER_ADMIN | Xoá giáo viên khỏi lớp | Header `X-Auth-Token`; Body `{"teacherIds":[...long...]}` | 200: `ClassResponse` (sau khi xoá) |
| `/classes/{classId}/lessons` | GET | SUPER_ADMIN hoặc TEACHER của lớp | Danh sách bài học trong lớp | Header `X-Auth-Token` | 200: `LessonResponse[]` |
| `/classes/{classId}/lessons/{lessonId}` | GET | SUPER_ADMIN hoặc TEACHER của lớp | Chi tiết bài học + tài nguyên | Header `X-Auth-Token` | 200: `LessonResponse` |
| `/classes/{classId}/lessons` | POST | SUPER_ADMIN hoặc TEACHER của lớp | Tạo bài học mới | Header `X-Auth-Token`; Body `{"title", optional: "description","orderIndex"}` | 200: `LessonResponse` |
| `/classes/{classId}/lessons/{lessonId}` | PUT | SUPER_ADMIN hoặc TEACHER của lớp | Cập nhật bài học | Header `X-Auth-Token`; Body optional `{"title","description","orderIndex"}` | 200: `LessonResponse` |
| `/classes/{classId}/lessons/{lessonId}` | DELETE | SUPER_ADMIN hoặc TEACHER của lớp | Xoá bài học | Header `X-Auth-Token` | 200 empty |
| `/classes/{classId}/lessons/{lessonId}/resources` | GET | SUPER_ADMIN hoặc TEACHER của lớp | Danh sách tài nguyên của bài học | Header `X-Auth-Token` | 200: `LessonResourceResponse[]` |
| `/classes/{classId}/lessons/{lessonId}/resources` | POST | SUPER_ADMIN hoặc TEACHER của lớp | Thêm tài nguyên | Header `X-Auth-Token`; Body `{"type","title","content","url","filePath"}` | 200: `LessonResourceResponse` |
| `/classes/{classId}/lessons/{lessonId}/resources/{resourceId}` | PUT | SUPER_ADMIN hoặc TEACHER của lớp | Cập nhật tài nguyên | Header `X-Auth-Token`; Body optional `{"type","title","content","url","filePath"}` | 200: `LessonResourceResponse` |
| `/classes/{classId}/lessons/{lessonId}/resources/{resourceId}` | DELETE | SUPER_ADMIN hoặc TEACHER của lớp | Xoá tài nguyên | Header `X-Auth-Token` | 200 empty |
| `/calendar/events` | GET | SUPER_ADMIN/TEACHER/STUDENT | Lịch tổng hợp (deadline assignment): admin thấy tất cả, teacher thấy lớp mình dạy, student thấy khoá đã đăng ký | Header `X-Auth-Token` | 200: `CalendarEventResponse[]` (mỗi event: id, title, description, deadline, classId/name, courseId/name, weight, createdBy, createdAt) |
| `/assignments/{assignmentId}/submissions` | POST | STUDENT của khoá chứa lớp của assignment | Nộp bài: Body `{"content","fileUrl"}` | 200: `SubmissionResponse` |
| `/assignments/{assignmentId}/submissions/{submissionId}` | GET | SUPER_ADMIN, TEACHER của lớp, hoặc chính STUDENT nộp bài | Xem bài nộp | 200: `SubmissionResponse` |
| `/assignments/{assignmentId}/submissions/{submissionId}/grade` | PUT | SUPER_ADMIN hoặc TEACHER của lớp | Chấm điểm/feedback | Body `{"score","feedback","status"}` | 200: `SubmissionResponse` |

Ghi chú:
- Context-path `/api`, ví dụ login: `POST /api/auth/login`.
- Header `X-Auth-Token` chứa raw token trả từ login.
- `UserResponse` trường: `id, username, firstName, lastName, fullName, emailAddress, emailVisibility, city, country, timezone, description, interest, phoneNumber, avatarUrl, active, admin, role, createdAt, lastLoginAt`.
- `CourseResponse` trường: `id, code, name, level, description, active, createdBy, createdAt`. Trường `active` map cột `status` trong DB: “active”/“inactive”.
- Roles: `role` một trong `SUPER_ADMIN`, `TEACHER`, `STUDENT`. Chỉ super_admin được tạo/sửa/xoá khoá học, đổi role người dùng, xem user theo id, thêm/xoá học viên.
