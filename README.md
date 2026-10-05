# HomeworkHub

## 1. Purpose

A small app where a teacher keeps a list of their students, gives homework (PDF) to individual students, and students upload solutions (one PDF, or photos) from their own panel.

## 2. Scope

**In scope (v1):** teacher and student accounts, student profiles (name, school class), assignments with PDF, assigning work to students with a deadline, a panel per student, submissions (latest only), manual grading, feedback.

**Later (not in v1):**
- Assignment library for the teacher (list of all assignments with per-student status, edit, take back, duplicate)
- Groups: "assign to a group" would just create one assignment work per member
- "Promote everyone to the next school class" button
- AI grading via the `SubmissionChecker` interface
- Cloud storage via the `FileStorage` interface

## 3. Actors

| Actor | Can do |
|-------|--------|
| TEACHER | Add and edit students, see the student list, open any student's panel, create assignments, give them to students, see submissions, grade, give feedback, extend a student's deadline |
| STUDENT | Open own panel, download assignment PDFs, upload and resubmit own solutions, see own grades and feedback |

## 4. Features

Status: `[ ]` todo, `[~]` in progress, `[x]` done

- [ ] Log in (session-based first, JWT later)
- [ ] Teacher adds a student (creates the account and profile)
- [ ] Teacher edits a student's profile (name, school class)
- [ ] Teacher sees the student list: name, school class, short summary (paginated)
- [ ] Teacher clicks a student to open that student's panel
- [ ] Teacher creates an assignment (title, description, PDF)
- [ ] Teacher gives an assignment to one or more students with a deadline (from the student's panel, or by picking students)
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
- School class is optional text (for example `7A`); the teacher updates it by hand
- Personal data is minimal and visible only to the teacher and the student themselves

### Assignment
- Title must not be blank
- Attachment must be a PDF, max size configurable
- An assignment has no deadline of its own; the deadline is set when it is given to students

### Assignment work
- An assignment can be given to one or more students; a student gets it at most once
- A deadline is required and must be in the future when the work is given
- Assignment work is open until its deadline (`isOpenAt(now)`)
- The teacher can move the deadline for one student
- Status is derived (not submitted, submitted, late, graded), not stored

### Submission
- Submitted on time -> not late
- Submitted after the deadline -> late *(decision pending: accept late work or reject it?)*
- Resubmission before the deadline replaces the previous submission; only the latest one is kept, there is no version history
- On resubmission the whole file set is replaced, the old files are deleted from storage, and `submittedAt` is updated
- Resubmission after the deadline is rejected (`DeadlinePassedException`)
- A graded submission cannot be changed
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
- A student can only open their own panel, profile and read their own submissions
- A teacher can open any student's profile and panel and read all submissions
- Access to another student's data returns 404 (not 403)
- A student sees only the teacher's name, never the teacher's email or any other student's data
- The list of allowed actions in the panel is a convenience for the UI; every action endpoint checks permissions again
- There is no public sign-up: the teacher creates student accounts
- Passwords are stored with BCrypt

## 6. Domain model and database scheme

### Tables

```
users
  id                  bigint        PK, generated
  email               varchar(255)  NOT NULL, UNIQUE
  password_hash       varchar(255)  NOT NULL
  first_name          varchar(100)  NOT NULL
  last_name           varchar(100)  NOT NULL
  role                varchar(20)   NOT NULL, CHECK in ('TEACHER','STUDENT')
  created_at          timestamptz   NOT NULL
  updated_at          timestamptz   NOT NULL

student_profile                                   -- only for users with role STUDENT
  user_id             bigint        PK and FK -> users(id) ON DELETE CASCADE
  school_class        varchar(20)   NULL

assignment
  id                  bigint        PK, generated
  title               varchar(200)  NOT NULL
  description         text          NULL
  pdf_file_key        varchar(255)  NOT NULL
  created_at          timestamptz   NOT NULL
  created_by          bigint        NOT NULL, FK -> users(id) ON DELETE RESTRICT

assignment_work
  id                  bigint        PK, generated
  assignment_id       bigint        NOT NULL, FK -> assignment(id) ON DELETE RESTRICT
  student_id          bigint        NOT NULL, FK -> student_profile(user_id) ON DELETE CASCADE
  deadline            timestamptz   NOT NULL
  assigned_at         timestamptz   NOT NULL
  UNIQUE (assignment_id, student_id)
  INDEX (student_id)

submission
  id                  bigint        PK, generated
  assignment_work_id  bigint        NOT NULL, UNIQUE, FK -> assignment_work(id) ON DELETE CASCADE
  submitted_at        timestamptz   NOT NULL
  is_late             boolean       NOT NULL
  grade_value         smallint      NULL, CHECK (0 to 100)
  grade_feedback      text          NULL
  graded_at           timestamptz   NULL
  CHECK ((grade_value IS NULL) = (graded_at IS NULL))

submission_file
  id                  bigint        PK, generated
  submission_id       bigint        NOT NULL, FK -> submission(id) ON DELETE CASCADE
  storage_key         varchar(255)  NOT NULL, UNIQUE
  original_filename   varchar(255)  NOT NULL
  file_type           varchar(10)   NOT NULL, CHECK in ('PDF','JPEG','PNG')
  size_bytes          bigint        NOT NULL
  position            smallint      NOT NULL
  UNIQUE (submission_id, position)
```

### Relationships (cardinality)

| Relationship | Reads as |
|---|---|
| users - student_profile | A student user has exactly one profile; a teacher has none (1 to 0..1) |
| users - assignment (created_by) | A teacher has zero or many assignments; each has exactly one creator |
| assignment - assignment_work | An assignment has zero or many assignment works (zero = not given yet); each belongs to exactly one assignment |
| student_profile - assignment_work | A student has zero or many assignment works; each belongs to exactly one student |
| assignment_work - submission | An assignment work has zero or one submission; a submission belongs to exactly one assignment work |
| submission - submission_file | A submission has 1 to 10 files; each file belongs to exactly one submission |

## 7. Panel view

Same view, different capabilities.

**Teacher: student list** (`/api/students`)
- Paginated list: first name, last name, school class
- Click a student to open their panel

**Teacher: a student's panel** (`/api/students/{studentId}/panel`)
- Header: the teacher's own name, then the student's profile (name, school class),
- The student's assigned work with status, deadline and grade; paginated, with a status filter
- Teacher actions: give a new assignment, `VIEW_SUBMISSION`, `DOWNLOAD_SUBMISSION`, `GRADE`, `EXTEND_DEADLINE`

**Student: own panel** (`/api/me/panel`)
- Header: the student's own name, the teacher's name (first and last name only),
- Own assigned work with status, deadline and grade; each item shows who gave it ("Given by ...")
- Actions: `DOWNLOAD_ASSIGNMENT`, `SUBMIT`, `RESUBMIT`, `VIEW_SUBMISSION`

## 8. Package structure (by feature)

```
com.yourname.homeworkhub
├── HomeworkHubApplication
├── user/          User, Role, StudentProfile, repos, UserService,
│                  StudentController, dto/
├── assignment/    Assignment, AssignedWork, repos, service, controller, dto/
├── submission/    Submission, SubmissionFile, FileType, SubmissionStatus (derived), Grade,
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

## 9. API draft

| Method | Path | Who | Notes                                  |
|--------|------|-----|----------------------------------------|
| POST | `/api/students` | Teacher | create student account + profile       |
| PUT | `/api/students/{studentId}` | Teacher | edit name, school class                |
| GET | `/api/students` | Teacher | paginated list with summary            |
| GET | `/api/students/{studentId}/panel` | Teacher | paginated, `?status=`                  |
| GET | `/api/me/panel` | Student | paginated, own assigned work           |
| POST | `/api/assignments` | Teacher | multipart: data + PDF                  |
| GET | `/api/assignments` | Teacher | simple list (id, title) for the picker |
| GET | `/api/assignments/{id}/file` | Assigned student / Teacher | PDF download                           |
| POST | `/api/assignments/{id}/assign` | Teacher | body: student ids + deadline           |
| PUT | `/api/assignment-work/{id}/deadline` | Teacher | extension for one student              |
| POST | `/api/assignment-work/{id}/submissions` | Student | multipart: `files` (list)              |
| GET | `/api/submissions/{id}` | Owner / Teacher |                                        |
| GET | `/api/submissions/{id}/files/{fileId}` | Owner / Teacher | one file (PDF or image)                |
| PUT | `/api/submissions/{id}/grade` | Teacher | body: value, feedback                  |
| GET | `/api/me/submissions` | Student | Slice, history                         |

## 10. Task board

### Step 1: Plain Java domain (no Spring, no JPA)
- [ ] Create project at start.spring.io (Web, Validation, JPA, H2), first commit
- [ ] `AssignedWork` + tests (deadline required, `isOpenAt`, extension)
- [ ] `Assignment` + tests (title, no deadline)
- [ ] `FileType` enum (PDF, JPEG, PNG) and `SubmissionFile` value object
- [ ] `Grade` record + tests
- [ ] `Submission` + tests (on time, late, resubmit, graded is locked), static factory, derived status
- [ ] File-set rules + tests: one PDF, or 1 to 10 images, no mixing
- [ ] `HomeworkHubException`, `DeadlinePassedException`

### Step 2: Spring Boot + REST, in-memory storage
- [ ] Controllers and DTO records for students, assignments and submissions
- [ ] Services with constructor injection
- [ ] `Clock` bean
- [ ] Create student, give assignment to students

### Step 3: JPA + H2
- [ ] Write the schema as a Flyway migration (`V1__init.sql`) or let Hibernate generate it first and compare
- [ ] Add JPA annotations to entities (`Grade` as `@Embedded`)
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
- [ ] Cleanup: save new files first, delete old ones last; remove saved files if a later one fails

### Step 6: Validation and error handling
- [ ] `GlobalExceptionHandler` with ProblemDetail
- [ ] Validation error list
- [ ] Catch-all 500 handler (log cause, hide details)
- [ ] `@WebMvcTest` asserting error JSON

### Step 7: Transactions
- [ ] `@Transactional` on submit
- [ ] Failure scenario: files saved, DB fails
- [ ] Self-invocation demo
- [ ] `@Version` for optimistic locking (student resubmits while teacher grades)

### Step 8: Security
- [ ] Session login, BCrypt, roles
- [ ] `@PreAuthorize` + ownership checks
- [ ] 401/403 as ProblemDetail
- [ ] `@WithMockUser` tests

### Step 9: Extras
- [ ] Assignment library for the teacher
- [ ] "Promote everyone to the next school class"
- [ ] PostgreSQL + Docker
- [ ] JWT
- [ ] `SubmissionChecker` AI implementation (suggest only, teacher approves)
- [ ] ZIP download of a submission for the teacher
- [ ] ArchUnit