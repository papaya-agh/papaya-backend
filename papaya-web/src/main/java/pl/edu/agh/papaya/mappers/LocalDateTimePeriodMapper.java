package pl.edu.agh.papaya.mappers;

import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.LocalDateTimePeriodDto;
import pl.edu.agh.papaya.model.LocalDateTimePeriod;

@Component
public class LocalDateTimePeriodMapper implements Mapper<LocalDateTimePeriod, LocalDateTimePeriodDto> {

    @Override
    public LocalDateTimePeriodDto mapToApi(LocalDateTimePeriod localDateTimePeriod) {
        return new LocalDateTimePeriodDto()
                .start(localDateTimePeriod.getStart())
                .end(localDateTimePeriod.getEnd());
    }

    public LocalDateTimePeriod mapFromApi(LocalDateTimePeriodDto periodDto) {
        var period = new LocalDateTimePeriod();
        period.set(periodDto.getStart(), periodDto.getEnd());
        return period;
    }
}
