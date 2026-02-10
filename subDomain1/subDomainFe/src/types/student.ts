export interface Student {
    id: number;
    studentCode: string;
    name: string;
    email: string;
    birthDate: string;
    gender: 'MALE' | 'FEMALE';
    score: number;
}

export interface StudentSearchReq {
    keyword?: string;
    level?: 'TRUNG_BINH' | 'KHA' | 'GIOI';
    page?: number;
    size?: number;
}

export interface PageResponse<T> {
    data: T[];
    total: number;
    page: number;
    size: number;
}

export interface CreateStudentReq {
    studentCode: string;
    name: string;
    email: string;
    birthDate: string;
    gender: 'MALE' | 'FEMALE';
    score: number;
}

export interface UpdateStudentReq {
    email: string;
    score: number;
}
