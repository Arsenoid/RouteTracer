alter table tracking_sessions
    add column if not exists route_id bigint,
    add constraint fk_session_route
        foreign key (route_id) references routes (id);

create index if not exists idx_sessions_route
    on tracking_sessions (route_id, started_at desc);
