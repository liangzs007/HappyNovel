alter table site_config
    add column if not exists rate_limit_per_minute integer not null default 30,
    add column if not exists max_concurrency integer not null default 1,
    add column if not exists chapter_list_selector text not null default '.chapter-list a',
    add column if not exists chapter_body_selector text not null default '.chapter-content',
    add column if not exists ad_blocklist text not null default '';

alter table book_source
    add column if not exists book_title varchar(255) not null default '',
    add column if not exists last_checked_at_epoch_minutes bigint;

alter table book_source
    alter column book_id drop not null;
