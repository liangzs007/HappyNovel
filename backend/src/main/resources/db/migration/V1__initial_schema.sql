create table if not exists site_config (
    id uuid primary key,
    name varchar(120) not null,
    base_domain varchar(255) not null,
    enabled boolean not null default true,
    created_at timestamptz not null default now()
);

create table if not exists book (
    id uuid primary key,
    title varchar(255) not null,
    author varchar(255) not null,
    cover_url text,
    description text,
    serialization_status varchar(40) not null,
    publication_status varchar(40) not null,
    recommendation_weight integer not null default 0,
    updated_at timestamptz not null default now()
);

create table if not exists book_source (
    id uuid primary key,
    book_id uuid not null references book(id),
    site_config_id uuid not null references site_config(id),
    source_url text not null,
    update_interval_minutes integer not null default 360,
    created_at timestamptz not null default now()
);

create table if not exists chapter (
    id uuid primary key,
    book_id uuid not null references book(id),
    chapter_order integer not null,
    title varchar(255) not null,
    crawl_status varchar(40) not null,
    clean_status varchar(40) not null,
    translation_status varchar(40) not null,
    publication_status varchar(40) not null,
    updated_at timestamptz not null default now(),
    unique(book_id, chapter_order)
);

create table if not exists chapter_raw_content (
    chapter_id uuid primary key references chapter(id),
    raw_content text not null,
    fetched_at timestamptz not null default now()
);

create table if not exists chapter_clean_content (
    chapter_id uuid primary key references chapter(id),
    title varchar(255) not null,
    paragraphs jsonb not null,
    quality_status varchar(40) not null,
    updated_at timestamptz not null default now()
);

create table if not exists chapter_translation (
    id uuid primary key,
    chapter_id uuid not null references chapter(id),
    language varchar(16) not null,
    title varchar(255) not null,
    paragraphs jsonb not null,
    provider varchar(80) not null,
    model varchar(120) not null,
    input_tokens integer not null default 0,
    output_tokens integer not null default 0,
    estimated_cost numeric(12, 6) not null default 0,
    publication_status varchar(40) not null,
    updated_at timestamptz not null default now(),
    unique(chapter_id, language)
);

create table if not exists taxonomy_category (
    id uuid primary key,
    name varchar(120) not null,
    slug varchar(120) not null unique,
    enabled boolean not null default true
);

create table if not exists taxonomy_tag (
    id uuid primary key,
    name varchar(120) not null,
    slug varchar(120) not null unique,
    enabled boolean not null default true
);

create table if not exists book_tag (
    book_id uuid not null references book(id),
    tag_id uuid not null references taxonomy_tag(id),
    primary key(book_id, tag_id)
);

create table if not exists glossary_term (
    id uuid primary key,
    book_id uuid not null references book(id),
    source_term varchar(255) not null,
    translated_term varchar(255) not null,
    term_type varchar(40) not null,
    description text,
    enabled boolean not null default true
);

create table if not exists pending_glossary_term (
    id uuid primary key,
    book_id uuid not null references book(id),
    chapter_id uuid references chapter(id),
    source_term varchar(255) not null,
    suggested_translation varchar(255),
    occurrence_count integer not null default 1,
    status varchar(40) not null
);

create table if not exists pipeline_task (
    id uuid primary key,
    task_type varchar(60) not null,
    status varchar(40) not null,
    target_type varchar(60) not null,
    target_id varchar(120) not null,
    priority integer not null default 0,
    payload jsonb not null default '{}'::jsonb,
    retry_count integer not null default 0,
    max_retries integer not null default 3,
    failure_reason text,
    started_at timestamptz,
    finished_at timestamptz,
    created_at timestamptz not null default now()
);

create table if not exists admin_user (
    id uuid primary key,
    username varchar(120) not null unique,
    password_hash varchar(255) not null,
    role varchar(80) not null,
    enabled boolean not null default true,
    created_at timestamptz not null default now()
);

create table if not exists audit_log (
    id uuid primary key,
    actor varchar(120) not null,
    action varchar(120) not null,
    target_type varchar(80) not null,
    target_id varchar(120) not null,
    summary text not null,
    created_at timestamptz not null default now()
);

create table if not exists anonymous_device (
    id uuid primary key,
    device_key varchar(120) not null unique,
    created_at timestamptz not null default now()
);

create table if not exists reading_event (
    id uuid primary key,
    device_id uuid references anonymous_device(id),
    book_id uuid references book(id),
    chapter_id uuid references chapter(id),
    event_type varchar(60) not null,
    created_at timestamptz not null default now()
);

create table if not exists ad_config (
    id uuid primary key,
    scope_type varchar(40) not null,
    scope_value varchar(120) not null,
    enabled boolean not null default true,
    reader_banner_enabled boolean not null default true,
    interstitial_every_chapters integer not null default 5
);

create table if not exists compliance_config (
    id uuid primary key,
    privacy_policy_url text,
    terms_url text,
    ad_disclosure text,
    updated_at timestamptz not null default now()
);

create table if not exists copyright_complaint (
    id uuid primary key,
    complainant varchar(255),
    contact varchar(255),
    book_id uuid references book(id),
    chapter_id uuid references chapter(id),
    description text not null,
    status varchar(40) not null,
    created_at timestamptz not null default now()
);

create index if not exists idx_book_publication_status on book(publication_status, updated_at);
create index if not exists idx_chapter_book_order on chapter(book_id, chapter_order);
create index if not exists idx_chapter_translation_state on chapter_translation(language, publication_status);
create index if not exists idx_pipeline_task_state on pipeline_task(task_type, status, priority, created_at);
create index if not exists idx_audit_log_target on audit_log(target_type, target_id, created_at);
