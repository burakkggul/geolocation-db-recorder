package tr.com.burakgul.geolocationdbrecorder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tr.com.burakgul.geolocationdbrecorder.model.Geolocation;

@Repository
public interface GeolocationRepository extends CrudRepository<Geolocation, Long> {
}
