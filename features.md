# HomeworkHub

## 1. Purpose

A small app where a teacher keeps a list of their students, gives homework (PDF) to individual students, and students upload solutions (one PDF, or photos) from their own panel.

## 2. Scope

**In scope (v1):** teacher and student accounts, student profiles (name, age, lesson times), assignments with PDF, assigning work to students, a panel per student, submissions (latest only), manual grading, feedback.

**Later (not in v1):**
- Assignment library for the teacher (list of all assignments with per-student status, edit, take back, duplicate)
- Groups: "assign to a group" would just create one assigned work per member
- AI grading via the `SubmissionChecker` interface
- Cloud storage via the `FileStorage` interface
- Lesson calendar features (reschedule, cancel, attendance), notifications, frontend, multiple teachers

## 3. Actors

| Actor | Can do |
|-------|--------|
| TEACHER | Add and edit students, see the student list, open any student's panel, create assignments, give them to students, see submissions, grade, give feedback, extend a student's deadline |
| STUDENT | Open own panel, download assignment PDFs, upload and resubmit own solutions, see own grades and feedback, see own profile |

## 4. Features

Status: `[ ]` todo, `[~]` in progress, `[x]` done

- [ ] Log in (session-based first, JWT later)
- [ ] Teacher adds a student (creates the account and profile)
- [ ] Teacher edits a student's profile and lesson times
- [ ] Teacher sees the student list: name, age, lesson times, short summary (paginated)
- [ ] Teacher clicks a student to open that student's panel
- [ ] Teacher creates an assignment (title, description, default deadline, PDF)
- [ ] Teacher gives an assignment to a student (from the student's panel), or to several students
- [ ] Teacher changes the deadline for one student (extension)
- [ ] Student panel: own assigned work with status and deadline (paginated)
- [ ] Student downloads assignment PDF
- [ ] Student uploads a submission: one PDF, or 1 to 10 photos (JPEG/PNG)
- [ ] Student resubmits before the deadline (replaces the previous files)
- [ ] Teacher views and downloads submission files, in page order
- [ ] Teacher grades a submission with feedback
- [ ] Student sees own grade and feedback
- [ ] Student sees a list of own submissions across assignments (Slice, "load more")

## 5. Business rules

Each rule should have at least one test.

### Student profile
- First name and last name must not be blank (both roles have them)
- Date of birth is stored; age is calculated from it and the current date (never stored), and the date cannot be in the future
- A student has zero or more weekly lesson slots: day of week, start time, duration in minutes
- Duration must be positive; a student's own lesson slots must not overlap
- Lesson times are local to the teacher's time zone (configured once)
- Personal data is minimal and visible only to the teacher and the student themselves
- A student sees only the teacher's name, never the teacher's email or any other student's data

### Assignment
- Title must not be blank
- Default deadline must not be null
- Attachment must be a PDF, max size configurable

### Assigned work
- An assignment can be given to one or more students; a student gets it at most once
- The deadline starts as the assignment's deadline and can be overridden per student
- Assigned work is open until its deadline (`isOpenAt(now)`)
- Status is derived from the submission and the deadline (not submitted, submitted, late, graded), not stored separately

### Submission
- Submitted on time -> status `SUBMITTED`
- Submitted after the deadline -> status `LATE` *(decision pending: accept late work or reject it?)*
- Resubmission before the deadline replaces the previous submission; only the latest one is kept, there is no version history
- Resubmission after the deadline is rejected (`DeadlinePassedException`)
- A `GRADED` submission cannot be changed
- On resubmission the whole file set is replaced, the old files are deleted from storage, and `submittedAt` is updated to the new time
- Student id and timestamp come from the server (auth + `Clock`), never from the request

### Files
- A submission is either exactly one PDF, or 1 to 10 images (JPEG or PNG); mixed uploads are rejected
- File type is detected from the content (magic bytes), never from the filename or the `Content-Type` header
- HEIC and any other type are rejected with 415 and a message to use JPEG
- Size limits (configurable): image max 10 MB, PDF max 20 MB, whole request max 50 MB
- File order is preserved (`position`)
- Every stored file gets a server-generated name; the original filename is only metadata
- Images are re-encoded on upload (orientation applied, then EXIF stripped); pixel dimensions are checked before decoding

### Grade
- Value between 0 and 100 inclusive
- Feedback is optional
- Only a teacher can grade

### Authorization
- A student can only open their own panel and profile and read their own submissions
- A teacher can open any student's profile and panel and read all submissions
- Access to another student's data returns 404 (not 403)
- The list of allowed actions in the panel is a convenience for the UI; every action endpoint checks permissions again
- There is no public sign-up: the teacher creates student accounts
- Passwords are stored with BCrypt

## 6. Domain model (v1)

```
User            id, email, passwordHash, role (TEACHER | STUDENT),
                firstName, lastName
StudentProfile  userId, dateOfBirth, lessonSlots (0..n)
LessonSlot      dayOfWeek, startTime, durationMinutes        [value object]
Assignment      id, title, description, defaultDeadline, pdfFileKey, createdBy
AssignedWork    id, assignment, student, deadline, assignedAt
Submission      id, assignedWork, submittedAt, status,
                grade (optional), files (1..10)
SubmissionFile  id, storageKey, originalFilename, fileType (PDF | JPEG | PNG),
                sizeBytes, position
Grade           value (0-100), feedback, gradedAt            [value object]
```


Statuses: `SUBMITTED -> GRADED`, `LATE -> GRADED`

The assignment is the template (written once). Assigned work is one copy per student, so a deadline extension or a missing submission is tracked per student.

## 7. Panel view

Same view, different capabilities.

**Teacher: student list** (`/api/students`)
- Paginated list: first name, last name, age, lesson times
- Short summary per student: open assignments, waiting to be graded, overdue
- Click a student to open their panel

**Teacher: a student's panel** (`/api/students/{studentId}/panel`)
- Header: the teacher's own name, then the student's profile (name, age, lesson times)
- The student's assigned work with status, deadline and grade; paginated, with a status filter
- Teacher actions: give a new assignment, `VIEW_SUBMISSION`, `DOWNLOAD_SUBMISSION`, `GRADE`, `EXTEND_DEADLINE`

**Student: own panel** (`/api/me/panel`)
- Header: the student's own name and the teacher's name (first and last name only)
- Own assigned work with status, deadline and grade; each item shows who gave it ("Given by ...")
- Actions: `DOWNLOAD_ASSIGNMENT`, `SUBMIT`, `RESUBMIT`, `VIEW_SUBMISSION`

**How it works**
- A small `PanelPermissions` class decides the allowed actions from the role, the status, and the deadline (easy to unit-test)
- The panel is a read-side view combining user, assignment and submission data
- Hiding a button is not security; the action endpoints check permissions themselves
- A student changing `{studentId}` in the URL must get 404; this needs a test
- Giving an assignment to a student needs a plain list of the teacher's assignments (title only) to pick from; the full assignment library comes later

## 8. Package structure (by feature)

```
com.yourname.homeworkhub
├── HomeworkHubApplication
├── user/          User, Role, StudentProfile, LessonSlot, repos,
│                  UserService, StudentController, dto/
├── assignment/    Assignment, AssignedWork, repos, service, controller, dto/
├── submission/    Submission, SubmissionFile, FileType, SubmissionStatus, Grade,
│                  UploadPolicy, repo, service, controller,
│                  SubmissionMapper (package-private), dto/ (incl. grade DTOs)
├── panel/         PanelService, PanelController, PanelPermissions, Action enum, dto/
├── grading/       SubmissionChecker (interface), ManualChecker, ai/ (later)
├── storage/       FileStorage (interface), LocalFileStorage, StorageProperties,
│                  ContentTypeDetector, ImageSanitizer
├── security/      SecurityConfig, auth classes
└── common/
    ├── error/     HomeworkHubException, NotFoundException,
    │              DeadlinePassedException, GlobalExceptionHandler
    └── config/    TimeConfig (Clock bean), PageResponse
```

### Dependency rules
```
panel      -> submission -> assignment -> user
submission -> storage
grading    -> submission
```
- One direction only, no cycles
- Talk to other features through their service or interface, never their repository
- Package-private by default; `public` only when another feature needs it
- Never return entities from controllers; use record DTOs

## 9. API draft

| Method | Path | Who | Notes |
|--------|------|-----|-------|
| POST | `/api/students` | Teacher | create student account + profile |
| PUT | `/api/students/{studentId}` | Teacher | edit profile and lesson times |
| GET | `/api/students` | Teacher | paginated list with age, lessons, summary |
| GET | `/api/students/{studentId}/panel` | Teacher | paginated, `?status=` |
| GET | `/api/me/panel` | Student | paginated, own assigned work |
| POST | `/api/assignments` | Teacher | multipart: data + PDF |
| GET | `/api/assignments` | Teacher | simple list (id, title) for the picker |
| GET | `/api/assignments/{id}/file` | Assigned student / Teacher | PDF download |
| POST | `/api/assignments/{id}/assign` | Teacher | body: student ids |
| PUT | `/api/assigned-work/{id}/deadline` | Teacher | extension for one student |
| POST | `/api/assigned-work/{id}/submissions` | Student | multipart: `files` (list) |
| GET | `/api/submissions/{id}` | Owner / Teacher | |
| GET | `/api/submissions/{id}/files/{fileId}` | Owner / Teacher | one file (PDF or image) |
| PUT | `/api/submissions/{id}/grade` | Teacher | body: value, feedback |
| GET | `/api/me/submissions` | Student | Slice, history |

## 10. Task board

### Step 1: Plain Java domain (no Spring, no JPA)
- [ ] Create project at start.spring.io (Web, Validation, JPA, H2), first commit
- [ ] `Assignment` + tests
- [ ] `AssignedWork` + tests (deadline override, `isOpenAt`)
- [ ] `LessonSlot` value object + tests (positive duration, overlap check)
- [ ] `StudentProfile` + tests (age from date of birth and a given date, no future birth date)
- [ ] `FileType` enum (PDF, JPEG, PNG) and `SubmissionFile` value object
- [ ] `SubmissionStatus` enum
- [ ] `Submission` + tests, static factory
- [ ] File-set rules + tests: one PDF, or 1 to 10 images, no mixing
- [ ] `Grade` record + tests
- [ ] `HomeworkHubException`, `DeadlinePassedException`

### Step 2: Spring Boot + REST, in-memory storage
- [ ] Controllers and DTO records for students, assignments and submissions
- [ ] Services with constructor injection
- [ ] `Clock` bean
- [ ] Create student, give assignment to students

### Step 3: JPA + H2
- [ ] Add JPA annotations to entities (`LessonSlot` as embeddable collection)
- [ ] Repositories
- [ ] `@DataJpaTest` tests
- [ ] Pagination: student list, student panel, history (Slice)
- [ ] N+1 experiment (student list with summary is a good candidate)

### Step 4: Panel
- [ ] `PanelPermissions` + unit tests per role and status
- [ ] Teacher student list with summary
- [ ] Teacher opens a student's panel
- [ ] Student panel endpoint
- [ ] Test: student cannot open another student's panel

### Step 5: File upload and download
- [ ] `FileStorage` interface + `LocalFileStorage`
- [ ] `StorageProperties` (limits)
- [ ] Upload with `List<MultipartFile>` and download
- [ ] `ContentTypeDetector` (magic bytes, or Apache Tika) + tests with real sample files
- [ ] `UploadPolicy`: type, count and size limits, 413/415 as ProblemDetail
- [ ] `ImageSanitizer`: orientation, strip EXIF, pixel size check
- [ ] Multipart limits in `application.properties` (max-file-size 20MB, max-request-size 50MB)
- [ ] `@TempDir` tests, filename and path traversal protection
- [ ] Cleanup of already-saved files if a later file fails

### Step 6: Validation and error handling
- [ ] `GlobalExceptionHandler` with ProblemDetail
- [ ] Validation error list
- [ ] Catch-all 500 handler (log cause, hide details)
- [ ] `@WebMvcTest` asserting error JSON

### Step 7: Transactions
- [ ] `@Transactional` on submit
- [ ] Failure scenario: files saved, DB fails
- [ ] Self-invocation demo

### Step 8: Security
- [ ] Session login, BCrypt, roles
- [ ] `@PreAuthorize` + ownership checks
- [ ] 401/403 as ProblemDetail
- [ ] `@WithMockUser` tests

### Step 9: Extras
- [ ] Assignment library for the teacher
- [ ] Filter the student list by lesson day ("who do I teach today?")
- [ ] PostgreSQL + Docker
- [ ] JWT
- [ ] `SubmissionChecker` AI implementation (suggest only, teacher approves)
- [ ] ZIP download of a submission for the teacher
- [ ] ArchUnit

## 11. DB scheme
