insert into site_config(id, name, base_domain, enabled)
values (
    '00000000-0000-0000-0000-000000000010',
    'MVP Seed Site',
    'https://novels.example.com',
    true
)
on conflict (id) do nothing;

insert into book(id, title, author, cover_url, description, serialization_status, publication_status, recommendation_weight, updated_at)
values (
    '00000000-0000-0000-0000-000000000101',
    'Dragon Gate',
    'Happy Novel Team',
    'https://example.com/covers/dragon-gate.jpg',
    'A translated cultivation novel prepared for MVP API validation.',
    'ongoing',
    'published',
    100,
    '2026-06-08T00:00:00Z'
)
on conflict (id) do update set
    title = excluded.title,
    author = excluded.author,
    cover_url = excluded.cover_url,
    description = excluded.description,
    serialization_status = excluded.serialization_status,
    publication_status = excluded.publication_status,
    recommendation_weight = excluded.recommendation_weight,
    updated_at = excluded.updated_at;

insert into book_source(id, book_id, site_config_id, source_url, update_interval_minutes)
values (
    '00000000-0000-0000-0000-000000000111',
    '00000000-0000-0000-0000-000000000101',
    '00000000-0000-0000-0000-000000000010',
    'https://novels.example.com/book/dragon-gate',
    360
)
on conflict (id) do nothing;

insert into taxonomy_category(id, name, slug, enabled)
values (
    '00000000-0000-0000-0000-000000000301',
    'Fantasy',
    'fantasy',
    true
)
on conflict (slug) do update set
    name = excluded.name,
    enabled = excluded.enabled;

insert into book_category(book_id, category_id)
values (
    '00000000-0000-0000-0000-000000000101',
    '00000000-0000-0000-0000-000000000301'
)
on conflict (book_id, category_id) do nothing;

insert into chapter(id, book_id, chapter_order, title, crawl_status, clean_status, translation_status, publication_status, updated_at)
values
    (
        '00000000-0000-0000-0000-000000000201',
        '00000000-0000-0000-0000-000000000101',
        1,
        '第一章 青云宗',
        'SUCCEEDED',
        'PASSED',
        'SUCCEEDED',
        'published',
        '2026-06-08T00:00:00Z'
    ),
    (
        '00000000-0000-0000-0000-000000000202',
        '00000000-0000-0000-0000-000000000101',
        2,
        '第二章 入门试炼',
        'SUCCEEDED',
        'PASSED',
        'SUCCEEDED',
        'published',
        '2026-06-08T00:00:00Z'
    )
on conflict (id) do update set
    title = excluded.title,
    crawl_status = excluded.crawl_status,
    clean_status = excluded.clean_status,
    translation_status = excluded.translation_status,
    publication_status = excluded.publication_status,
    updated_at = excluded.updated_at;

insert into chapter_translation(
    id,
    chapter_id,
    language,
    title,
    paragraphs,
    provider,
    model,
    input_tokens,
    output_tokens,
    estimated_cost,
    publication_status,
    updated_at
)
values
    (
        '00000000-0000-0000-0000-000000000401',
        '00000000-0000-0000-0000-000000000201',
        'en',
        'Chapter 1: Azure Cloud Sect',
        '["The morning bell echoed across Azure Cloud Sect.", "Lin Chen stepped through the Dragon Gate for the first time."]'::jsonb,
        'openai',
        'gpt-5-mini',
        120,
        64,
        0.000100,
        'published',
        '2026-06-08T00:00:00Z'
    ),
    (
        '00000000-0000-0000-0000-000000000402',
        '00000000-0000-0000-0000-000000000202',
        'en',
        'Chapter 2: The Trial',
        '["The first trial began before sunrise.", "The outer disciples gathered beneath the old pine trees."]'::jsonb,
        'openai',
        'gpt-5-mini',
        118,
        61,
        0.000100,
        'published',
        '2026-06-08T00:00:00Z'
    )
on conflict (chapter_id, language) do update set
    title = excluded.title,
    paragraphs = excluded.paragraphs,
    provider = excluded.provider,
    model = excluded.model,
    input_tokens = excluded.input_tokens,
    output_tokens = excluded.output_tokens,
    estimated_cost = excluded.estimated_cost,
    publication_status = excluded.publication_status,
    updated_at = excluded.updated_at;

insert into glossary_term(id, book_id, source_term, translated_term, term_type, description, enabled)
values (
    '00000000-0000-0000-0000-000000000501',
    '00000000-0000-0000-0000-000000000101',
    '青云宗',
    'Azure Cloud Sect',
    'ORGANIZATION',
    '主角初入的宗门',
    true
)
on conflict (id) do update set
    source_term = excluded.source_term,
    translated_term = excluded.translated_term,
    term_type = excluded.term_type,
    description = excluded.description,
    enabled = excluded.enabled;
