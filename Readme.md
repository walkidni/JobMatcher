# JobMatcher

A modern, full-stack job matching platform for candidates and recruiters.  
Built with React, TailwindCSS, Spring Boot, and PostgreSQL.

## 🚀 Features

### For Candidates
- Register, login, and manage your profile
- Upload, preview, and delete your resume (PDF/DOC/DOCX)
- See jobs matched to your resume using smart skill matching

### For Recruiters
- Register, login, and manage your profile
- Post new jobs with required skills
- View and manage your posted jobs
- See matched candidates for each job (with resume preview and match score)

### General
- JWT authentication & role-based access
- Clean, responsive UI with TailwindCSS
- RESTful API with Spring Boot
- PostgreSQL database
- Modern React (Context API, hooks, modular pages)
- Error handling and feedback throughout

## 🖥️ Tech Stack

- **Frontend:** React, Vite, TailwindCSS, Axios
- **Backend:** Spring Boot, Java, Spring Security, JPA/Hibernate
- **Database:** PostgreSQL
- **Authentication:** JWT (JSON Web Token)

## 📦 Project Structure

```
jobmatcher/
├── src/
│   ├── main/
│   │   ├── java/                  # Spring Boot backend code
│   │   └── resources/             # Backend resources (application.yml, etc.)
│   └── test/                      # Backend tests
├── frontend/
│   ├── src/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── context/
│   │   ├── lib/
│   │   ├── pages/
│   │   ├── stores/                # (May be empty/unused)
│   │   ├── index.css
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── public/
│   ├── package.json
│   ├── tailwind.config.js
│   ├── vite.config.ts
│   └── ...
├── README.md
├── pom.xml                        # Maven config for backend
└── ...
```

## 🛠️ Setup & Run

### 1. Backend

- Configure your PostgreSQL database in `src/main/resources/application.yml`
- Build and run the Spring Boot app:

```sh
cd backend
./mvnw spring-boot:run
```

### 2. Frontend

- Install dependencies and start the dev server:

```sh
cd frontend
npm install
npm run dev
```

- The frontend will be available at [http://localhost:5173](http://localhost:5173)

## 🔑 Environment Variables

- Backend: Configure DB and JWT secret in `application.yml`
- Frontend: No special env vars needed for local dev (uses Vite proxy to `/api`)

## 📝 Usage

- Register as a candidate or recruiter
- Candidates: upload your resume, see matched jobs
- Recruiters: post jobs, view your jobs, see matched candidates for each job

## 📸 Screenshots

> _Add screenshots here for login, dashboard, job posting, resume upload, etc._

## 🤝 Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.

## 📄 License

MIT

## 👤 Author

- [Walid KINI](https://github.com/walkidni)
