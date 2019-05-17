package person.crud.Service

import grails.gorm.services.Service
import groovy.transform.CompileStatic
import person.crud.Model.Person

/**
 * Created by chris on 17/05/19.
 */

@CompileStatic
@Service(Person)
abstract class PersonService {

    abstract int count()
    abstract List<Person> findAll()
    abstract List<Person> findAll(Map args)
    abstract Person find(Long id)
    abstract Person save(Person person)
    abstract Person update(Long id, String firstName, String lastName)
    abstract Person delete(Long id)
}
