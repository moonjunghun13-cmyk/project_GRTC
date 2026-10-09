package com.grtc.main.admin.timetable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableRepository extends JpaRepository<TimetableEntity, Long> {

    List<TimetableEntity> findAllByDayTypeOrderBySeqAsc(DayType dayType);

    List<TimetableEntity> findAllByDayTypeAndMoveTypeOrderBySeqAsc(DayType dayType, MoveType moveType);
}
