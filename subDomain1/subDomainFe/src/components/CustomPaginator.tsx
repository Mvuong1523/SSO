import React from 'react';
import { Button } from 'primereact/button';
import { classNames } from 'primereact/utils';

interface CustomPaginatorProps {
    first: number;
    rows: number;
    totalRecords: number;
    onPageChange: (page: number) => void;
}

const CustomPaginator: React.FC<CustomPaginatorProps> = ({ first, rows, totalRecords, onPageChange }) => {
    const currentPage = Math.floor(first / rows) + 1;
    const totalPages = Math.ceil(totalRecords / rows);

    if (totalPages <= 1) return null;

    const renderPageNumbers = () => {
        const pages = [];


        let startPage = Math.max(1, currentPage - 2);
        let endPage = Math.min(totalPages, startPage + 4);

        if (endPage - startPage < 4) {
            startPage = Math.max(1, endPage - 4);
        }

        if (startPage > 1) {
            pages.push(
                <Button key="start_ellipsis" label="..." className="p-button-text" disabled />
            );
        }

        for (let i = startPage; i <= endPage; i++) {
            const isActive = i === currentPage;
            pages.push(
                <Button
                    key={i}
                    label={i.toString()}
                    className={classNames('p-button-sm')}
                    outlined={!isActive}
                    onClick={() => onPageChange(i)}
                    style={{
                        fontWeight: isActive ? 'bold' : 'normal',
                        minWidth: '2.5rem'
                    }}
                />
            );
        }


        if (endPage < totalPages) {
            pages.push(
                <Button key="end_ellipsis" label="..." className="p-button-text" disabled />
            );
        }

        return pages;
    };

    return (
        <div className="flex align-items-center justify-content-center gap-2 mt-3">
            <Button
                icon="pi pi-angle-double-left"
                className="p-button-sm"
                outlined
                onClick={() => onPageChange(1)}
                disabled={currentPage === 1}
            />
            <Button
                icon="pi pi-angle-left"
                className="p-button-sm"
                outlined
                onClick={() => onPageChange(currentPage - 1)}
                disabled={currentPage === 1}
            />

            {renderPageNumbers()}

            <Button
                icon="pi pi-angle-right"
                className="p-button-sm"
                outlined
                onClick={() => onPageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
            />
            <Button
                icon="pi pi-angle-double-right"
                className="p-button-sm"
                outlined
                onClick={() => onPageChange(totalPages)}
                disabled={currentPage === totalPages}
            />
        </div>
    );
};

export default CustomPaginator;
