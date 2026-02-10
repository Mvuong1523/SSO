const fs = require('fs');

const generateStudents = (count) => {
    let sql = `-- Create Database (if not exists)
CREATE DATABASE IF NOT EXISTS subdomain_db;
USE subdomain_db;

-- Create Student Table
DROP TABLE IF EXISTS student;
CREATE TABLE student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    score DOUBLE NOT NULL
);

-- Insert Dummy Data
INSERT INTO student (student_code, name, email, birth_date, gender, score) VALUES
`;

    const firstNames = ['Nguyen', 'Tran', 'Le', 'Pham', 'Hoang', 'Vu', 'Vo', 'Dang', 'Bui', 'Do', 'Ho', 'Ngo', 'Duong', 'Ly'];
    const middleNames = ['Van', 'Thi', 'Minh', 'Huu', 'Duc', 'Thanh', 'Quoc', 'Tuan', 'Hai', 'Phuong'];
    const lastNames = ['An', 'Binh', 'Cuong', 'Dung', 'Em', 'Hai', 'Giang', 'Hieu', 'Khanh', 'Lam', 'Minh', 'Nam', 'Oanh', 'Phuc', 'Quang', 'Son', 'Thuy', 'Uyen', 'Vinh', 'Yen'];

    for (let i = 1; i <= count; i++) {
        const firstName = firstNames[Math.floor(Math.random() * firstNames.length)];
        const middleName = middleNames[Math.floor(Math.random() * middleNames.length)];
        const lastName = lastNames[Math.floor(Math.random() * lastNames.length)];
        const fullName = `${firstName} ${middleName} ${lastName} ${i}`; // Add number to ensure uniqueness if logic fails, but code is unique

        const code = `SV${String(i).padStart(3, '0')}`;
        const email = `student${i}@example.com`;

        // Random date between 1995 and 2005
        const year = 1995 + Math.floor(Math.random() * 11);
        const month = String(1 + Math.floor(Math.random() * 12)).padStart(2, '0');
        const day = String(1 + Math.floor(Math.random() * 28)).padStart(2, '0');
        const birthDate = `${year}-${month}-${day}`;

        const gender = Math.random() > 0.5 ? 'MALE' : 'FEMALE';
        const score = (Math.random() * 10).toFixed(1);

        sql += `('${code}', '${fullName}', '${email}', '${birthDate}', '${gender}', ${score})`;

        if (i < count) {
            sql += ',\n';
        } else {
            sql += ';\n';
        }
    }

    return sql;
};

const sqlContent = generateStudents(200);
fs.writeFileSync('init_student_db.sql', sqlContent);
console.log('Generated init_student_db.sql with 200 records');
