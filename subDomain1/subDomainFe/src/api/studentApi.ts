import axiosClient from './axiosClient';
import type { Student, StudentSearchReq, CreateStudentReq, UpdateStudentReq, PageResponse } from '../types/student';

interface ApiResponse<T> {
    result: T;
    message?: string;
    code?: number;
}

export const studentApi = {
    search: async (params: StudentSearchReq): Promise<PageResponse<Student>> => {
        const response = await axiosClient.post<ApiResponse<PageResponse<Student>>>('/students/search', params);
        return response.data.result;
    },

    create: async (data: CreateStudentReq): Promise<string> => {
        const response = await axiosClient.post<ApiResponse<string>>('/students', data);
        return response.data.message || 'Create success';
    },

    update: async (code: string, data: UpdateStudentReq): Promise<string> => {
        const response = await axiosClient.put<ApiResponse<string>>(`/students/${code}`, data);
        return response.data.message || 'Update success';
    },

    delete: async (code: string): Promise<string> => {
        const response = await axiosClient.delete<ApiResponse<string>>(`/students/${code}`);
        return response.data.message || 'Delete success';
    }
};
