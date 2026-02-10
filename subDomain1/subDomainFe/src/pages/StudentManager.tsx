import { useState, useEffect, useRef } from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Toast } from 'primereact/toast';
import { Dropdown } from 'primereact/dropdown';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import './StudentManager.css';
import { studentApi } from '../api/studentApi';
import type { Student, CreateStudentReq, UpdateStudentReq, PageResponse } from '../types/student';
import { classNames } from 'primereact/utils';
import CustomPaginator from '../components/CustomPaginator';
import LogoutButton from '../components/LogoutButton';

const defaultFormData: any = {
    studentCode: '',
    name: '',
    email: '',
    birthDate: '',
    gender: 'MALE',
    score: null
};

const StudentManager = () => {
    // --- State ---
    const [showDialog, setShowDialog] = useState(false);
    const [isEdit, setIsEdit] = useState(false);
    const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
    const [formData, setFormData] = useState<CreateStudentReq>(defaultFormData);
    const [submitted, setSubmitted] = useState(false);
    const toast = useRef<Toast>(null);
    const [students, setStudents] = useState<Student[]>([]);
    const [totalRecords, setTotalRecords] = useState(0);
    const [loading, setLoading] = useState(false);

    const [queryParams, setQueryParams] = useState({
        keyword: '',
        level: 'ALL',
        page: 1,
        size: 10,
        trigger: 0
    });
    const [touched, setTouched] = useState<Record<string, boolean>>({});

    useEffect(() => {
        loadStudents();
    }, [queryParams.page, queryParams.trigger, queryParams.level]);

    const loadStudents = async () => {
        setLoading(true);
        try {
            const searchParams: any = {
                keyword: queryParams.keyword,
                page: queryParams.page,
                size: queryParams.size
            };

            if (queryParams.level) {
                searchParams.level = queryParams.level;
            }

            const response: PageResponse<Student> = await studentApi.search(searchParams);
            setStudents(response.data);
            setTotalRecords(response.total);
        } catch (error) {
            toast.current?.show({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải dữ liệu' });
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e: any) => {
        const { name, value } = e.target;
        setQueryParams(prev => ({
            ...prev,
            [name]: value,
            ...(name === 'level' ? { page: 1, trigger: prev.trigger + 1 } : {})
        }));
    };

    const onSearch = () => {
        setQueryParams(prev => ({
            ...prev,
            page: 1,
            trigger: prev.trigger + 1
        }));
    };

    const handleFormChange = (e: any) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };


    const openNew = () => {
        setFormData(defaultFormData);
        setSubmitted(false);
        setTouched({});
        setIsEdit(false);
        setShowDialog(true);
    };

    const openEdit = (student: Student) => {
        setFormData({
            studentCode: student.studentCode,
            name: student.name,
            email: student.email,
            birthDate: student.birthDate,
            gender: student.gender,
            score: student.score
        });
        setSelectedStudent(student);
        setSubmitted(false);
        setTouched({});
        setIsEdit(true);
        setShowDialog(true);
    };

    const hideDialog = () => {
        setSubmitted(false);
        setTouched({});
        setShowDialog(false);
    };

    const onBlur = (field: string) => {
        setTouched(prev => ({ ...prev, [field]: true }));
    };

    const validateEmail = (email: string) => {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    };

    const validateBirthDate = (dateString: string) => {
        if (!dateString) return false;
        const date = new Date(dateString);
        return date < new Date();
    };

    const validate = (data: any) => {
        const errors: Record<string, string> = {};

        if (!data.studentCode.trim()) errors.studentCode = 'Code is required.';
        if (!data.name.trim()) errors.name = 'Name is required.';

        if (!data.email.trim()) {
            errors.email = 'Email is required.';
        } else if (!validateEmail(data.email)) {
            errors.email = 'Email chưa đúng định dạng.';
        }

        if (!data.birthDate) {
            errors.birthDate = 'Birth Date is required.';
        } else if (!validateBirthDate(data.birthDate)) {
            errors.birthDate = 'Ngày sinh phải là quá khứ';
        }

        if (data.score === null || data.score === undefined) {
            errors.score = 'Score is required.';
        } else if (data.score < 0 || data.score > 10) {
            errors.score = 'Điểm trong khoảng từ 0 đến 10.';
        }

        return errors;
    };

    const validationErrors = validate(formData);
    const hasErrors = Object.keys(validationErrors).length > 0;

    const getErrorMessage = (field: keyof typeof validationErrors) => {
        return (submitted || touched[field]) ? validationErrors[field] : '';
    };

    const confirmSave = () => {
        setSubmitted(true);
        if (hasErrors) {
            return;
        }

        confirmDialog({
            message: 'Bạn có chắc chắn muốn lưu sinh viên này?',
            header: 'Xác nhận',
            icon: 'pi pi-exclamation-triangle',
            accept: () => {
                saveStudent()
                setQueryParams(prev => ({ ...prev, trigger: prev.trigger + 1 }));
            }
        });
    };
    const saveStudent = async () => {
        try {
            if (isEdit && selectedStudent) {
                const updateData: UpdateStudentReq = {
                    email: formData.email,
                    score: formData.score
                };
                await studentApi.update(selectedStudent.studentCode, updateData);
                toast.current?.show({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật sinh viên' });
            } else {
                await studentApi.create(formData);
                toast.current?.show({ severity: 'success', summary: 'Thành công', detail: 'Đã tạo sinh viên mới' });
            }
            setShowDialog(false);
            setQueryParams(prev => ({ ...prev, trigger: prev.trigger + 1 }));
        } catch (error: any) {
            toast.current?.show({ severity: 'error', summary: 'Lỗi', detail: error.response?.data?.message || 'Thao tác thất bại' });
        }
    };

    const confirmDelete = (student: Student) => {
        confirmDialog({
            message: `Bạn có chắc chắn muốn xóa ${student.name} (${student.studentCode})?`,
            header: 'Xác nhận xóa',
            icon: 'pi pi-info-circle',
            acceptClassName: 'p-button-danger',
            accept: () => {
                deleteStudent(student)
                setQueryParams(prev => ({ ...prev, trigger: prev.trigger + 1 }));
            }
        });
    };

    const deleteStudent = async (student: Student) => {
        try {
            await studentApi.delete(student.studentCode);
            toast.current?.show({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa sinh viên' });
            if (students.length === 1 && queryParams.page > 1) {
                setQueryParams(prev => ({ ...prev, page: prev.page - 1 }));
            } else {
                setQueryParams(prev => ({ ...prev, trigger: prev.trigger + 1 }));
            }
        } catch (error) {
            toast.current?.show({ severity: 'error', summary: 'Lỗi', detail: 'Xóa thất bại' });
        }
    };

    const getRowBackgroundColor = (score: number): string => {
        if (score >= 8.0) return '#d4edda'; // xanh lá nhạt
        if (score >= 6.5) return '#fff3cd'; // vàng nhạt
        return '#f8d7da'; // đỏ nhạt
    };

    const getStudentLevel = (score: number) => {
        if (score >= 8.0) return 'Giỏi';
        if (score >= 6.5) return 'Khá';
        return 'Trung bình';
    };

    return (
        <div className="card p-4">
            <Toast ref={toast} />
            <ConfirmDialog />

            <DataTable
                value={students}
                rowClassName={(rowData: Student) => {
                    if (rowData.score >= 8.0) return 'bg-green-50';
                    if (rowData.score >= 6.5) return 'bg-yellow-50';
                    return 'bg-red-50';
                }}
                header={
                    <div className="flex flex-wrap align-items-center justify-content-between gap-2">
                        <div>
                            <span className="text-xl text-900 font-bold mr-3">Quản lý sinh viên</span>
                            <span className="text-600">Tổng số: <span className="font-bold text-primary">{totalRecords}</span></span>
                        </div>
                        <div className="flex gap-2">
                            <Dropdown
                                name="level"
                                value={queryParams.level}
                                options={[
                                    { label: 'Tất cả', value: 'ALL' },
                                    { label: 'Giỏi', value: 'GIOI' },
                                    { label: 'Khá', value: 'KHA' },
                                    { label: 'Trung Bình', value: 'TRUNG_BINH' }
                                ]}
                                onChange={handleChange}
                                className="w-10rem"
                            />
                            <div className="flex gap-2">
                                <InputText
                                    name="keyword"
                                    value={queryParams.keyword}
                                    onChange={handleChange}
                                    onKeyDown={(e) => e.key === 'Enter' && onSearch()}
                                    placeholder="Tìm kiếm..."
                                />
                                <Button label="Tìm" severity="info" icon="pi pi-search" onClick={onSearch} />
                            </div>
                            <Button label="Thêm mới" severity="info" icon="pi pi-plus" onClick={openNew} />
                            <LogoutButton />
                        </div>
                    </div>
                }
                loading={loading}
                tableStyle={{ minWidth: '50rem' }}
                paginator={false}
            >
                <Column
                    header="STT"
                    body={(rowData, options) => (queryParams.page - 1) * queryParams.size + options.rowIndex + 1}
                    style={{ width: '3rem' }}
                />
                <Column field="studentCode" header="Mã SV"></Column>
                <Column field="name" header="Họ tên"></Column>
                <Column field="email" header="Email"></Column>
                <Column field="birthDate" header="Ngày sinh"></Column>
                <Column 
                    field="gender" 
                    header="Giới tính"
                    body={(rowData: Student) => rowData.gender === 'MALE' ? 'Nam' : 'Nữ'}
                ></Column>
                <Column field="score" header="Điểm"></Column>
                <Column 
                    field="level" 
                    header="Xếp loại"
                    body={(rowData: Student) => getStudentLevel(rowData.score)}
                ></Column>
                <Column
                    header="Thao tác"
                    style={{ minWidth: '4rem' }}
                    body={(rowData: Student) => (
                        <div className="flex gap-2">
                            <Button icon="pi pi-pencil" rounded severity="info" onClick={() => openEdit(rowData)} />
                            <Button icon="pi pi-trash" rounded severity="danger" onClick={() => confirmDelete(rowData)} />
                        </div>
                    )}
                />
            </DataTable>

            <CustomPaginator
                first={(queryParams.page - 1) * queryParams.size}
                rows={queryParams.size}
                totalRecords={totalRecords}
                onPageChange={(page) => handleChange({ target: { name: 'page', value: page } })}
            />

            <Dialog visible={showDialog} style={{ width: '32rem' }} breakpoints={{ '960px': '75vw', '641px': '90vw' }} header="Thông tin sinh viên" modal className="p-fluid" onHide={hideDialog}>

                <div className="field">
                    <label htmlFor="studentCode" className="font-bold">Mã sinh viên</label>
                    <InputText id="studentCode" name="studentCode" value={formData.studentCode} onChange={handleFormChange} onBlur={() => onBlur('studentCode')} required disabled={isEdit} className={classNames({ 'p-invalid': !!getErrorMessage('studentCode') })} />
                    {getErrorMessage('studentCode') && <small className="p-error">{getErrorMessage('studentCode')}</small>}
                </div>

                <div className="field">
                    <label htmlFor="name" className="font-bold">Họ tên</label>
                    <InputText id="name" name="name" value={formData.name} onChange={handleFormChange} onBlur={() => onBlur('name')} required disabled={isEdit} className={classNames({ 'p-invalid': !!getErrorMessage('name') })} />
                    {getErrorMessage('name') && <small className="p-error">{getErrorMessage('name')}</small>}
                </div>

                <div className="field">
                    <label htmlFor="email" className="font-bold">Email</label>
                    <InputText id="email" name="email" value={formData.email} onChange={handleFormChange} onBlur={() => onBlur('email')} required className={classNames({ 'p-invalid': !!getErrorMessage('email') })} />
                    {getErrorMessage('email') && <small className="p-error">{getErrorMessage('email')}</small>}
                </div>

                <div className="field">
                    <label htmlFor="birthDate" className="font-bold">Ngày sinh</label>
                    <InputText id="birthDate" name="birthDate" type="date" value={formData.birthDate || ''} onChange={handleFormChange} onBlur={() => onBlur('birthDate')} required disabled={isEdit} className={classNames({ 'p-invalid': !!getErrorMessage('birthDate') })} />
                    {getErrorMessage('birthDate') && <small className="p-error">{getErrorMessage('birthDate')}</small>}
                </div>

                <div className="field">
                    <label htmlFor="gender" className="font-bold">Giới tính</label>
                    <select
                        id="gender"
                        name="gender"
                        value={formData.gender}
                        onChange={handleFormChange}
                        disabled={isEdit}
                        className="p-inputtext w-full"
                    >
                        <option value="MALE">Nam</option>
                        <option value="FEMALE">Nữ</option>
                    </select>
                </div>

                <div className="field">
                    <label htmlFor="score" className="font-bold">Điểm</label>
                    <InputNumber
                        id="score"
                        name="score"
                        value={formData.score}
                        onValueChange={(e) => handleFormChange({ target: { name: 'score', value: e.value } })}
                        onBlur={() => onBlur('score')}
                        mode="decimal"
                        min={0}
                        max={10}
                        minFractionDigits={1}
                        maxFractionDigits={2}
                        className={classNames({ 'p-invalid': !!getErrorMessage('score') })}
                    />
                    {getErrorMessage('score') && <small className="p-error">{getErrorMessage('score')}</small>}
                </div>

                {submitted && hasErrors && <div className="text-red-500 font-bold mb-2">Vui lòng điền đầy đủ và hợp lệ thông tin</div>}

                <div className="flex justify-content-end gap-2 mt-4">
                    <Button label="Hủy" icon="pi pi-times" outlined onClick={hideDialog} />
                    <Button label="Lưu" icon="pi pi-check" onClick={confirmSave} />
                </div>
            </Dialog>
        </div>
    );
}

export default StudentManager;
