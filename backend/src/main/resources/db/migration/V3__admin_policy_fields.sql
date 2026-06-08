alter table compliance_config
    add column if not exists privacy_policy_title varchar(255) not null default 'HappyNovel Privacy Policy',
    add column if not exists terms_title varchar(255) not null default 'HappyNovel Terms of Service',
    add column if not exists ad_disclosure_enabled boolean not null default true;

alter table copyright_complaint
    add column if not exists source varchar(120),
    add column if not exists book_title varchar(255),
    add column if not exists chapter_title varchar(255),
    add column if not exists note text;

create index if not exists idx_copyright_complaint_created_at on copyright_complaint(created_at desc);
