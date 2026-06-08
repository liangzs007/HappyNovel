create table if not exists book_category (
    book_id uuid not null references book(id),
    category_id uuid not null references taxonomy_category(id),
    primary key(book_id, category_id)
);

create index if not exists idx_book_category_category on book_category(category_id, book_id);
