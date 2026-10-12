package com.grtc.main.admin.operation;

import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperationService {

    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;
    private final OperationRepository operationRepository;

    public OperationDto.MapResponse map() {
        RouteEntity route = findRoute();
        List<StationEntity> stations = stationRepository.findAllByRouteIdOrderBySeqAsc(route.getId());

        List<OperationDto.StationInfo> stationInfos = stations.stream()
                .map(s -> new OperationDto.StationInfo(s.getId(), s.getSeq(), s.getName()))
                .toList();
        List<OperationDto.TrainInfo> trains = operationRepository.findAll().stream()
                .map(op -> toTrain(op, stations))
                .toList();

        return new OperationDto.MapResponse(toRouteInfo(route, stations), stationInfos, trains);
    }

    public OperationDto.RouteInfo routeInfo() {
        RouteEntity route = findRoute();
        return toRouteInfo(route, stationRepository.findAllByRouteIdOrderBySeqAsc(route.getId()));
    }

    private OperationDto.RouteInfo toRouteInfo(RouteEntity route, List<StationEntity> stations) {
        return new OperationDto.RouteInfo(
                route.getId(),
                route.getName(),
                route.getStatus(),
                route.getStatus().getLabel(),
                stations.size(),
                route.getDailyRunCount(),
                stations.isEmpty() ? null : stations.get(0).getName(),
                stations.isEmpty() ? null : stations.get(stations.size() - 1).getName()
        );
    }

    private OperationDto.TrainInfo toTrain(OperationEntity op, List<StationEntity> stations) {
        int currentIdx = indexOfId(stations, op.getCurrentStation().getId());
        int terminalIdx = indexOfName(stations, op.getDirection().getTerminalName());

        int step = (currentIdx < 0 || terminalIdx < 0) ? 0 : Integer.compare(terminalIdx, currentIdx);
        String next = step == 0 ? null : stations.get(currentIdx + step).getName();
        String heading = step > 0 ? "FORWARD" : (step < 0 ? "BACKWARD" : "ARRIVED");

        return new OperationDto.TrainInfo(
                op.getId(),
                op.getVehicle().getVehicleNo(),
                op.getDirection(),
                op.getDirection().getLabel(),
                op.getCurrentStation().getId(),
                op.getCurrentStation().getName(),
                next,
                heading
        );
    }

    private int indexOfId(List<StationEntity> stations, Long id) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private int indexOfName(List<StationEntity> stations, String name) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private RouteEntity findRoute() {
        return routeRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new BusinessException(ErrorCode.ROUTE_NOT_FOUND));
    }
}
