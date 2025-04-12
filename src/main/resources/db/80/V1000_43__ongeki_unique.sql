-- Delete duplicate rows

-- UserMusicDetailList
DELETE d
FROM ongeki_user_music_detail d
         LEFT JOIN (
    SELECT MAX(id) AS id
    FROM ongeki_user_music_detail
    GROUP BY user_id, music_id, level
) keep ON d.id = keep.id
WHERE keep.id IS NULL;

-- UserCharacterList
DELETE d
FROM ongeki_user_character d
         LEFT JOIN (
    SELECT MAX(id) AS id
    FROM ongeki_user_character
    GROUP BY user_id, character_id
) keep ON d.id = keep.id
WHERE keep.id IS NULL;

-- UserCardList
DELETE d
FROM ongeki_user_card d
         LEFT JOIN (
    SELECT MAX(id) AS id
    FROM ongeki_user_card
    GROUP BY user_id, card_id
) keep ON d.id = keep.id
WHERE keep.id IS NULL;

-- UserDeckList
DELETE d
FROM ongeki_user_deck d
         LEFT JOIN (
    SELECT MAX(id) AS id
    FROM ongeki_user_deck
    GROUP BY user_id, deck_id
) keep ON d.id = keep.id
WHERE keep.id IS NULL;

-- UserTrainingRoomList
DELETE d
FROM ongeki_user_training_room d
         LEFT JOIN (
    SELECT MAX(id) AS id
    FROM ongeki_user_training_room
    GROUP BY user_id, room_id
) keep ON d.id = keep.id
WHERE keep.id IS NULL;

-- UserChapterList
DELETE d
FROM ongeki_user_chapter d
         LEFT JOIN (
    SELECT MAX(id) AS id
    FROM ongeki_user_chapter
    GROUP BY user_id, chapter_id
) keep ON d.id = keep.id
WHERE keep.id IS NULL;

-- UserMemoryChapterList
DELETE d
FROM ongeki_user_memory_chapter d
         LEFT JOIN (
    SELECT MAX(id) AS id
    FROM ongeki_user_memory_chapter
    GROUP BY user_id, chapter_id
) keep ON d.id = keep.id
WHERE keep.id IS NULL;

-- UserItemList
DELETE d
FROM ongeki_user_item d
         LEFT JOIN (
    SELECT MAX(id) AS id
    FROM ongeki_user_item
    GROUP BY user_id, item_kind, item_id
) keep ON d.id = keep.id
WHERE keep.id IS NULL;

-- UserMusicItemList
DELETE d
FROM ongeki_user_music_item d
         LEFT JOIN (
    SELECT MAX(id) AS id
    FROM ongeki_user_music_item
    GROUP BY user_id, music_id
) keep ON d.id = keep.id
WHERE keep.id IS NULL;

-- UserLoginBonusList
DELETE d
FROM ongeki_user_login_bonus d
         LEFT JOIN (
    SELECT MAX(id) AS id
    FROM ongeki_user_login_bonus
    GROUP BY user_id, bonus_id
) keep ON d.id = keep.id
WHERE keep.id IS NULL;

-- UserEventPointList
DELETE d
FROM ongeki_user_event_point d
         LEFT JOIN (
    SELECT MAX(id) AS id
    FROM ongeki_user_event_point
    GROUP BY user_id, event_id
) keep ON d.id = keep.id
WHERE keep.id IS NULL;

-- UserMissionPointList
DELETE d
FROM ongeki_user_mission_point d
         LEFT JOIN (
    SELECT MAX(id) AS id
    FROM ongeki_user_mission_point
    GROUP BY user_id, event_id
) keep ON d.id = keep.id
WHERE keep.id IS NULL;

-- UserMusicDetailList
ALTER TABLE ongeki_user_music_detail
    ADD UNIQUE KEY uniq_user_music_level (user_id, music_id, level);

-- UserCharacterList
ALTER TABLE ongeki_user_character
    ADD UNIQUE KEY uniq_user_character (user_id, character_id);

-- UserCardList
ALTER TABLE ongeki_user_card
    ADD UNIQUE KEY uniq_user_card (user_id, card_id);

-- UserDeckList
ALTER TABLE ongeki_user_deck
    ADD UNIQUE KEY uniq_user_deck (user_id, deck_id);

-- UserTrainingRoomList
ALTER TABLE ongeki_user_training_room
    ADD UNIQUE KEY uniq_user_room (user_id, room_id);

-- UserChapterList
ALTER TABLE ongeki_user_chapter
    ADD UNIQUE KEY uniq_user_chapter (user_id, chapter_id);

-- UserMemoryChapterList
ALTER TABLE ongeki_user_memory_chapter
    ADD UNIQUE KEY uniq_user_mem_chapter (user_id, chapter_id);

-- UserItemList
ALTER TABLE ongeki_user_item
    ADD UNIQUE KEY uniq_user_item (user_id, item_kind, item_id);

-- UserMusicItemList
ALTER TABLE ongeki_user_music_item
    ADD UNIQUE KEY uniq_user_music_item (user_id, music_id);

-- UserLoginBonusList
ALTER TABLE ongeki_user_login_bonus
    ADD UNIQUE KEY uniq_user_login_bonus (user_id, bonus_id);

-- UserEventPointList
ALTER TABLE ongeki_user_event_point
    ADD UNIQUE KEY uniq_user_event_point (user_id, event_id);

-- UserMissionPointList
ALTER TABLE ongeki_user_mission_point
    ADD UNIQUE KEY uniq_user_mission_point (user_id, event_id);
