-- ============================================================================
-- AURA — Schéma de base de données Supabase (PostgreSQL)
-- À exécuter dans l'éditeur SQL de votre projet Supabase
-- ============================================================================

-- Extension UUID
create extension if not exists "uuid-ossp";

-- ============================================================================
-- TABLE: users
-- ============================================================================
create table public.users (
    id            uuid primary key references auth.users(id) on delete cascade,
    firstname     text not null,
    lastname      text not null,
    email         text unique not null,
    birthdate     date not null,
    password_hash text not null,
    username      text unique not null,
    avatar_url    text default '',
    bio           text default '',
    followers     integer default 0,
    following     integer default 0,
    posts         integer default 0,
    verified      boolean default false,
    created_at    timestamptz default now()
);

-- Index pour recherche rapide
create index idx_users_username on public.users using gin (username gin_trgm_ops);
create index idx_users_email    on public.users (email);

-- ============================================================================
-- TABLE: posts
-- ============================================================================
create table public.posts (
    id              uuid primary key default uuid_generate_v4(),
    user_id         uuid references public.users(id) on delete cascade,
    content         text default '',
    media_url       text default '',
    media_type      text default '',  -- 'image' | 'video' | ''
    likes_count     integer default 0,
    comments_count  integer default 0,
    shares_count    integer default 0,
    created_at      timestamptz default now()
);

create index idx_posts_user    on public.posts (user_id);
create index idx_posts_created on public.posts (created_at desc);

-- ============================================================================
-- TABLE: likes
-- ============================================================================
create table public.likes (
    id         uuid primary key default uuid_generate_v4(),
    user_id    uuid references public.users(id) on delete cascade,
    post_id    uuid references public.posts(id) on delete cascade,
    created_at timestamptz default now(),
    unique (user_id, post_id)
);

-- ============================================================================
-- TABLE: comments
-- ============================================================================
create table public.comments (
    id         uuid primary key default uuid_generate_v4(),
    post_id    uuid references public.posts(id) on delete cascade,
    user_id    uuid references public.users(id) on delete cascade,
    content    text not null,
    created_at timestamptz default now()
);

create index idx_comments_post on public.comments (post_id);

-- ============================================================================
-- TABLE: follows
-- ============================================================================
create table public.follows (
    id          uuid primary key default uuid_generate_v4(),
    follower_id uuid references public.users(id) on delete cascade,
    followed_id uuid references public.users(id) on delete cascade,
    created_at  timestamptz default now(),
    unique (follower_id, followed_id)
);

-- ============================================================================
-- TABLE: saves (publications sauvegardées)
-- ============================================================================
create table public.saves (
    id         uuid primary key default uuid_generate_v4(),
    user_id    uuid references public.users(id) on delete cascade,
    post_id    uuid references public.posts(id) on delete cascade,
    created_at timestamptz default now(),
    unique (user_id, post_id)
);

-- ============================================================================
-- TABLE: conversations
-- ============================================================================
create table public.conversations (
    id           uuid primary key default uuid_generate_v4(),
    user1_id     uuid references public.users(id) on delete cascade,
    user2_id     uuid references public.users(id) on delete cascade,
    last_message text default '',
    unread_count integer default 0,
    updated_at   timestamptz default now(),
    unique (user1_id, user2_id)
);

-- ============================================================================
-- TABLE: messages
-- ============================================================================
create table public.messages (
    id              uuid primary key default uuid_generate_v4(),
    conversation_id uuid references public.conversations(id) on delete cascade,
    sender_id       uuid references public.users(id) on delete cascade,
    content         text default '',
    media_url       text default '',
    is_read         boolean default false,
    created_at      timestamptz default now()
);

create index idx_messages_conv on public.messages (conversation_id);

-- ============================================================================
-- TABLE: notifications
-- ============================================================================
create table public.notifications (
    id         uuid primary key default uuid_generate_v4(),
    user_id    uuid references public.users(id) on delete cascade,
    sender_id  uuid references public.users(id) on delete cascade,
    type       text not null,  -- 'like' | 'comment' | 'follow' | 'mention'
    post_id    uuid references public.posts(id) on delete cascade,
    is_read    boolean default false,
    created_at timestamptz default now()
);

create index idx_notifications_user on public.notifications (user_id);

-- ============================================================================
-- TRIGGERS — mise à jour automatique des compteurs
-- ============================================================================

-- Compteur de likes
create or replace function update_likes_count() returns trigger as $$
begin
    if (TG_OP = 'INSERT') then
        update public.posts set likes_count = likes_count + 1 where id = NEW.post_id;
    elsif (TG_OP = 'DELETE') then
        update public.posts set likes_count = likes_count - 1 where id = OLD.post_id;
    end if;
    return null;
end;
$$ language plpgsql;

create trigger trg_likes_count
after insert or delete on public.likes
for each row execute function update_likes_count();

-- Compteur de commentaires
create or replace function update_comments_count() returns trigger as $$
begin
    if (TG_OP = 'INSERT') then
        update public.posts set comments_count = comments_count + 1 where id = NEW.post_id;
    elsif (TG_OP = 'DELETE') then
        update public.posts set comments_count = comments_count - 1 where id = OLD.post_id;
    end if;
    return null;
end;
$$ language plpgsql;

create trigger trg_comments_count
after insert or delete on public.comments
for each row execute function update_comments_count();

-- Compteurs followers / following
create or replace function update_follow_counts() returns trigger as $$
begin
    if (TG_OP = 'INSERT') then
        update public.users set following = following + 1 where id = NEW.follower_id;
        update public.users set followers = followers + 1 where id = NEW.followed_id;
    elsif (TG_OP = 'DELETE') then
        update public.users set following = following - 1 where id = OLD.follower_id;
        update public.users set followers = followers - 1 where id = OLD.followed_id;
    end if;
    return null;
end;
$$ language plpgsql;

create trigger trg_follow_counts
after insert or delete on public.follows
for each row execute function update_follow_counts();

-- Compteur de publications
create or replace function update_posts_count() returns trigger as $$
begin
    if (TG_OP = 'INSERT') then
        update public.users set posts = posts + 1 where id = NEW.user_id;
    elsif (TG_OP = 'DELETE') then
        update public.users set posts = posts - 1 where id = OLD.user_id;
    end if;
    return null;
end;
$$ language plpgsql;

create trigger trg_posts_count
after insert or delete on public.posts
for each row execute function update_posts_count();

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

alter table public.users         enable row level security;
alter table public.posts         enable row level security;
alter table public.comments      enable row level security;
alter table public.likes         enable row level security;
alter table public.follows       enable row level security;
alter table public.saves         enable row level security;
alter table public.messages      enable row level security;
alter table public.conversations enable row level security;
alter table public.notifications enable row level security;

-- Lecture publique des profils et publications
create policy "Public read users"  on public.users  for select using (true);
create policy "Public read posts"  on public.posts  for select using (true);
create policy "Public read comments" on public.comments for select using (true);
create policy "Public read likes"  on public.likes  for select using (true);
create policy "Public read follows" on public.follows for select using (true);

-- Écriture restreinte au propriétaire
create policy "Users update own profile" on public.users
    for update using (auth.uid() = id);

create policy "Users insert own profile" on public.users
    for insert with check (auth.uid() = id);

create policy "Users create posts" on public.posts
    for insert with check (auth.uid() = user_id);

create policy "Users delete own posts" on public.posts
    for delete using (auth.uid() = user_id);

create policy "Users create comments" on public.comments
    for insert with check (auth.uid() = user_id);

create policy "Users manage own likes" on public.likes
    for all using (auth.uid() = user_id);

create policy "Users manage own follows" on public.follows
    for all using (auth.uid() = follower_id);

create policy "Users manage own saves" on public.saves
    for all using (auth.uid() = user_id);

-- Messages : visibles uniquement par les participants
create policy "Participants read conversations" on public.conversations
    for select using (auth.uid() = user1_id or auth.uid() = user2_id);

create policy "Participants create conversations" on public.conversations
    for insert with check (auth.uid() = user1_id or auth.uid() = user2_id);

create policy "Participants read messages" on public.messages
    for select using (
        auth.uid() in (
            select user1_id from public.conversations where id = conversation_id
            union
            select user2_id from public.conversations where id = conversation_id
        )
    );

create policy "Participants send messages" on public.messages
    for insert with check (auth.uid() = sender_id);

-- Notifications : visibles uniquement par le destinataire
create policy "Users read own notifications" on public.notifications
    for select using (auth.uid() = user_id);

create policy "System creates notifications" on public.notifications
    for insert with check (true);

create policy "Users update own notifications" on public.notifications
    for update using (auth.uid() = user_id);

-- ============================================================================
-- STORAGE BUCKETS
-- ============================================================================
-- À créer manuellement dans Supabase Dashboard > Storage :
--   - avatars  (public)
--   - posts    (public)
--   - messages (public)

-- ============================================================================
-- EXTENSION pg_trgm (pour recherche ilike performante)
-- ============================================================================
create extension if not exists pg_trgm;

-- ============================================================================
-- REALTIME
-- ============================================================================
-- Activer Realtime sur les tables messages et notifications
-- via Supabase Dashboard > Database > Replication
alter publication supabase_realtime add table public.messages;
alter publication supabase_realtime add table public.notifications;
