import { useEffect, useState, useRef } from 'react'
import { DataTable } from 'primereact/datatable';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { Toast } from 'primereact/toast';
import { Column } from 'primereact/column';
import CustomPaginator from './components/CustomPaginator';
import type { Student } from './types/student';
import './App.css'
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';

function App() {
  const toast = useRef<Toast>(null)
  const [count, setCount] = useState(0)
  const [students, setStudents] = useState<Student[]>([])
  const [dialogMode, setDialogMode] = useState<"add" | "edit" | null>(null)
  const [loading, setLoading] = useState(false)
  const [totalRecords, setTotalRecords] = useState(0)
  const [touched, setTouched] = useState<Record<string, boolean>>({})
  const [formError, setFormError] = useState(false)
  const [student, setStudent] = useState({
    id: 0,
    code: "",
    name: "",
    email: "",
    gender: "MALE",
    dateOfBirth: "",
    grade: "GOOD"
  })
  const [search, setSearch] = useState({
    grade: "ALL",
    keyWord: "",
    page: 1,
    size: 10
  })

  const handleSearch = (event: any) => {
    setSearch({
      ...search,
      [event.target.name]: event.target.value
    })
    if (event.target.name === "grade") {
      setSearch((prev) => ({ ...prev, page: 1 }))
    }

  }

  const handleStudentChange = (event: any) => {
    const { name, value } = event.target
    setStudent({
      ...student,
      [name]: value
    })
    setTouched((prev) => ({ ...prev, [name]: true }))
  }

  const handleBlur = (field: string) => {
    setTouched((prev) => ({ ...prev, [field]: true }))
  }

  const handleClick = () => {
    setCount(count + 1)
    setSearch((prev) => ({ ...prev, page: 1 }))
  }

  const openAddDialog = () => {
    setStudent({
      id: 0,
      code: "",
      name: "",
      email: "",
      gender: "MALE",
      dateOfBirth: "",
      grade: "GOOD"
    })
    setDialogMode("add")
    setTouched({})
    setFormError(false)
  }

  const openEditDialog = (s: Student) => {
    setStudent({
      id: s.id,
      code: s.code,
      name: s.name,
      email: s.email,
      gender: s.gender,
      dateOfBirth: s.dateOfBirth,
      grade: s.grade
    })
    setDialogMode("edit")
    setTouched({})
    setFormError(false)
  }

  useEffect(() => {
    const fetchGet = async () => {
      try {
        setLoading(true)
        const response = await fetch(`http://localhost:8080/api/students/search`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(search),
        })
        const data = await response.json()
        setStudents(data.data.content)
        setTotalRecords(data.data.totalElements)
      } catch (error) {
        console.log(error)
      } finally {
        setLoading(false)
      }
    }
    fetchGet()
  }, [search.grade, search.page, search.size, count])

  const doSubmit = async () => {
    const isEdit = dialogMode === "edit"

    const requestBody = {
      id: student.id,
      studentCode: student.code,
      fullName: student.name,
      email: student.email,
      gender: student.gender,
      DOB: student.dateOfBirth,
      grade: student.grade
    }

    const response = await fetch("http://localhost:8080/api/students", {
      method: isEdit ? "PUT" : "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestBody),
    })
    const data = await response.json()
    if (data.success) {
      setDialogMode(null)
      setStudent({
        id: 0,
        code: "",
        name: "",
        email: "",
        gender: "MALE",
        dateOfBirth: "",
        grade: "GOOD"
      })
      setCount(count + 1)
      toast.current?.show({ severity: 'success', summary: 'Thành công', detail: dialogMode === 'edit' ? 'Cập nhật thành công!' : 'Thêm mới thành công!', life: 3000 })
    } else {
      toast.current?.show({ severity: 'error', summary: 'Lỗi', detail: data.message, life: 3000 })
    }
  }

  const handleSubmit = () => {
    const isFutureDate = student.dateOfBirth ? new Date(student.dateOfBirth) >= new Date() : false;
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const isInvalidEmail = student.email ? !emailRegex.test(student.email) : false;

    if (!student.code || !student.name || !student.email || !student.dateOfBirth || isFutureDate || isInvalidEmail) {
      setTouched({
        code: true,
        name: true,
        email: true,
        dateOfBirth: true
      })
      setFormError(true)
      return
    }

    const isEdit = dialogMode === "edit"
    confirmDialog({
      message: isEdit ? 'Bạn có chắc muốn cập nhật sinh viên này?' : 'Bạn có chắc muốn thêm sinh viên mới?',
      header: 'Xác nhận',
      acceptLabel: 'OK',
      rejectLabel: 'Hủy',
      accept: doSubmit
    })
  }

  const handleDelete = (e: Student) => {
    console.log(123)
    confirmDialog({
      message: `Bạn có chắc muốn xóa sinh viên ${e.name}?`,
      header: 'Xác nhận xóa',
      acceptLabel: 'Xóa',
      rejectLabel: 'Hủy',
      acceptClassName: 'p-button-danger',
      accept: async () => {
        const response = await fetch(`http://localhost:8080/api/students/${e.id}`, {
          method: "DELETE",
        })
        const data = await response.json()
        if (data.success) {
          setCount(count + 1)
          toast.current?.show({ severity: 'success', summary: 'Thành công', detail: 'Xóa sinh viên thành công!', life: 3000 })
        } else {
          toast.current?.show({ severity: 'error', summary: 'Lỗi', detail: data.message, life: 3000 })
        }
      }
    })
  }





  return (
    <>
      <Toast ref={toast} />
      <ConfirmDialog />
      <h1>Kết quả học tập</h1>
      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          name="keyWord"
          value={search.keyWord}
          onChange={handleSearch} />
        <select name="grade" value={search.grade} onChange={handleSearch}>
          <option value="ALL">Tất cả</option>
          <option value="EXCELLENT">Giỏi</option>
          <option value="GOOD">Khá</option>
          <option value="AVERAGE">Trung bình</option>
        </select>
        <Button label="Tìm kiếm" severity="info" onClick={handleClick} />
        <Button label="Thêm" severity="info" onClick={openAddDialog} style={{ marginLeft: '5px' }} />
      </div>

      <Dialog
        visible={dialogMode !== null}
        header={dialogMode === "add" ? "Thêm sinh viên" : "Sửa sinh viên"}
        onHide={() => setDialogMode(null)}
        style={{ width: '25rem', }}
      >
        <div className="form-group">
          <label>Mã học sinh <span style={{ color: 'red' }}>*</span></label>
          <InputText name="code" value={student.code}
            onChange={handleStudentChange}
            readOnly={dialogMode === "edit"}
            onBlur={() => handleBlur("code")}
            className={touched.code && !student.code ? 'p-invalid' : ''} />
          {touched.code && !student.code && <small className="p-error" style={{ color: 'red' }}>Mã học sinh là bắt buộc.</small>}
        </div>
        <div className="form-group">
          <label>Tên <span style={{ color: 'red' }}>*</span></label>
          <InputText name="name" value={student.name}
            onChange={handleStudentChange}
            readOnly={dialogMode === "edit"}
            onBlur={() => handleBlur("name")}
            className={touched.name && !student.name ? 'p-invalid' : ''} />
          {touched.name && !student.name && <small className="p-error" style={{ color: 'red' }}>Tên là bắt buộc.</small>}
        </div>
        <div className="form-group">
          <label>Email <span style={{ color: 'red' }}>*</span></label>
          <InputText name="email" value={student.email} onChange={handleStudentChange} onBlur={() => handleBlur("email")} className={touched.email && (!student.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(student.email)) ? 'p-invalid' : ''} />
          {touched.email && !student.email && <small className="p-error" style={{ color: 'red' }}>Email là bắt buộc.</small>}
          {touched.email && student.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(student.email) && <small className="p-error" style={{ color: 'red' }}>Email không đúng định dạng.</small>}
        </div>
        <div className="form-group">
          <label>Giới tính</label>
          <select name="gender" value={student.gender} onChange={handleStudentChange}>
            <option value="MALE">Nam</option>
            <option value="FEMALE">Nữ</option>
          </select>
        </div>
        <div className="form-group">
          <label>Ngày sinh <span style={{ color: 'red' }}>*</span></label>
          <input type="date" name="dateOfBirth"
            value={student.dateOfBirth}
            onChange={handleStudentChange}
            readOnly={dialogMode === "edit"}
            onBlur={() => handleBlur("dateOfBirth")}
            className={`p-inputtext p-component ${touched.dateOfBirth && (!student.dateOfBirth || new Date(student.dateOfBirth) >= new Date()) ? 'p-invalid' : ''}`} />
          {touched.dateOfBirth && !student.dateOfBirth && <div className="p-error" style={{ fontSize: '12px', marginTop: '4px', color: 'red' }}>Ngày sinh là bắt buộc.</div>}
          {touched.dateOfBirth && student.dateOfBirth && new Date(student.dateOfBirth) >= new Date() && <div className="p-error" style={{ fontSize: '12px', marginTop: '4px', color: 'red' }}>Ngày sinh phải ở quá khứ.</div>}
        </div>
        <div className="form-group">
          <label>Học lực <span style={{ color: 'red' }}>*</span></label>
          <select name="grade" value={student.grade} onChange={handleStudentChange} onBlur={() => handleBlur("grade")} className={touched.grade && !student.grade ? 'p-invalid' : ''}>
            <option value="EXCELLENT">Giỏi</option>
            <option value="GOOD">Khá</option>
            <option value="AVERAGE">Trung bình</option>
          </select>
          {touched.grade && !student.grade && <small className="p-error" style={{ color: 'red' }}>Học lực là bắt buộc.</small>}
        </div>
        {formError && <div style={{ color: 'red', textAlign: 'center', marginBottom: '10px' }}>Vui lòng điền đầy đủ và hợp lệ thông tin bắt buộc</div>}
        <div className="form-buttons">
          <Button label="Hủy" severity="secondary" onClick={() => setDialogMode(null)} />
          <Button label="Lưu" onClick={handleSubmit} />
        </div>
      </Dialog>

      <DataTable
        value={students}
        tableStyle={{ minWidth: '50rem' }}
        emptyMessage="Không có sinh viên nào"
        rowClassName={(data: Student) => {
          if (data.grade === 'EXCELLENT') return 'row-excellent'
          if (data.grade === 'GOOD') return 'row-good'
          return 'row-average'
        }}
      >
        <Column header="STT" body={(_: Student, options: { rowIndex: number }) => {
          return (search.page - 1) * search.size + options.rowIndex + 1
        }} />
        <Column field="code" header="Mã SV"></Column>
        <Column field="name" header="Họ tên"></Column>
        <Column field="email" header="Email"></Column>
        <Column
          field="gender"
          header="Giới tính"
          body={(row: Student) => {
            return row.gender === "MALE" ? "Nam" : "Nữ"
          }}
        ></Column>
        <Column field="dateOfBirth" header="Ngày sinh"
          body={(row: Student) => {
            if (!row.dateOfBirth) return ""
            const date = new Date(row.dateOfBirth)
            const day = date.getDate().toString().padStart(2, '0')
            const month = (date.getMonth() + 1).toString().padStart(2, '0')
            const year = date.getFullYear().toString().slice()
            return `${day}/${month}/${year}`
          }}
        />
        <Column
          field="grade"
          header="Học lực"
          body={(row: Student) => {
            if (row.grade === "EXCELLENT") return "Giỏi"
            if (row.grade === "GOOD") return "Khá"
            return "Trung bình"
          }}
        />
        <Column
          header="Hành động"
          body={(row: Student) => (
            <div>
              <Button label="Sửa" severity="info" size="small" onClick={() => openEditDialog(row)} />
              <Button label="Xóa" severity="danger" size="small" onClick={() => handleDelete(row)} style={{ marginLeft: '5px' }} />
            </div>
          )}
        />
      </DataTable>
      <CustomPaginator
        currentPage={search.page}
        totalPages={Math.ceil(totalRecords / search.size)}
        onPageChange={(page: number) => handleSearch({ target: { name: 'page', value: page } })}
      />
    </>
  )
}

export default App
