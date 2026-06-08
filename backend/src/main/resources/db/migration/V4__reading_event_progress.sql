alter table reading_event
    add column if not exists progress_percent real not null default 0;

create index if not exists idx_reading_event_book_created_at on reading_event(book_id, created_at desc);
