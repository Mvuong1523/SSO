import './CustomPaginator.css'

interface CustomPaginatorProps {
    currentPage: number
    totalPages: number
    onPageChange: (page: number) => void
    maxVisible?: number
}

export default function CustomPaginator({
    currentPage,
    totalPages,
    onPageChange,
    maxVisible = 5
}: CustomPaginatorProps) {

    const getPageNumbers = () => {
        const pages: (number | string)[] = []
        const half = Math.floor(maxVisible / 2)

        let startPage = Math.max(1, currentPage - half)
        let endPage = Math.min(totalPages, currentPage + half)

        if (currentPage <= half) {
            endPage = Math.min(totalPages, maxVisible)
        }
        if (currentPage > totalPages - half) {
            startPage = Math.max(1, totalPages - maxVisible + 1)
        }

        if (startPage > 1) {
            pages.push('...')
        }

        for (let i = startPage; i <= endPage; i++) {
            pages.push(i)
        }

        if (endPage < totalPages) {
            pages.push('...')
        }

        return pages
    }

    if (totalPages <= 1) return null

    return (
        <div className="custom-paginator">
            <button
                className="page-btn"
                onClick={() => onPageChange(1)}
                disabled={currentPage === 1}
            >
                «
            </button>
            <button
                className="page-btn"
                onClick={() => onPageChange(currentPage - 1)}
                disabled={currentPage === 1}
            >
                ‹
            </button>

            {getPageNumbers().map((page, index) => (
                page === '...' ? (
                    <span key={`ellipsis-${index}`} className="ellipsis">...</span>
                ) : (
                    <button
                        key={page}
                        className={`page-btn ${currentPage === page ? 'active' : ''}`}
                        onClick={() => onPageChange(page as number)}
                        disabled={currentPage === page}
                    >
                        {page}
                    </button>
                )
            ))}

            <button
                className="page-btn"
                onClick={() => onPageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
            >
                ›
            </button>
            <button
                className="page-btn"
                onClick={() => onPageChange(totalPages)}
                disabled={currentPage === totalPages}
            >
                »
            </button>
        </div>
    )
}
