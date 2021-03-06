package person.crud.Controller

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.reactivex.Single
import org.springframework.validation.FieldError
import person.crud.Model.Person
import person.crud.Service.PersonService
import javax.annotation.Nullable
import javax.inject.Inject
import org.grails.datastore.mapping.validation.ValidationException

/**
 * Created by chris on 17/05/19.
 */

@CompileStatic
@Transactional
@Controller('/person')
class PersonController {

    @Get('/')
    HttpResponse index() {
        HttpResponse.ok()
    }

    @Inject
    PersonService personService

    @Get('/list{?offset}{?max}')
    List<Person> getPeople(@Nullable Optional<Integer> offset, Optional<Integer> max) {
        if (offset && max) {
            personService.findAll([offset:offset.get(), max:max.get()])
        } else {
            Person.list()
        }
    }

    @Get('/count')
    Integer getCount() {
        personService.count()
    }

    @Get('/get/{id}')
    Person getPerson(Integer id) {
        personService.find(id)
    }

    @Put('/save')
    HttpResponse<Map> savePerson(@Body Person person) {
        try {
            return HttpResponse.ok( [person: personService.save(person)] as Map )
        } catch (ValidationException e) {
            return HttpResponse.unprocessableEntity().body(
                    [
                            person: person,
                            errors: e.errors.allErrors.collect {
                                FieldError err = it as FieldError
                                [
                                        field: err.field,
                                        rejectedValud: err.rejectedValue,
                                        message: err.defaultMessage
                                ]
                            }
                    ]
            ) as HttpResponse<Map>
        }
    }

    @Put('/saveAll')
    HttpResponse<Map> savePersons(@Body List<Person> persons) {
        try {
            return HttpResponse.ok( [persons: persons.each {personService.save(it)}] as Map )
        } catch (ValidationException e) {
            return HttpResponse.unprocessableEntity().body(
                    [
                            persons: persons,
                            errors: e.errors.allErrors.collect {
                                FieldError err = it as FieldError
                                [
                                        field: err.field,
                                        rejectedValud: err.rejectedValue,
                                        message: err.defaultMessage
                                ]
                            }
                    ]
            ) as HttpResponse<Map>
        }
    }

    @Patch('/update')
    HttpResponse<Map> updatePerson(@Body Person person) {
        try {
            return HttpResponse.ok( [person: personService.update(person.id, person.firstName, person.lastName)] as Map )
        } catch (ValidationException e) {
            return HttpResponse.unprocessableEntity().body(
                    [
                            person: person,
                            errors: e.errors.allErrors.collect {
                                FieldError err = it as FieldError
                                [
                                        field: err.field,
                                        rejectedValud: err.rejectedValue,
                                        message: err.defaultMessage
                                ]
                            }
                    ]
            ) as HttpResponse<Map>
        }
    }

    @Delete('/delete/{id}')
    HttpResponse<Map> deletePerson(@Body Integer id) {
        try {
            return HttpResponse.ok( [person: personService.delete(id)] as Map )
        } catch (ValidationException e) {
            return HttpResponse.unprocessableEntity().body(
                    [
                            person: id,
                            errors: e.errors.allErrors.collect {
                                FieldError err = it as FieldError
                                [
                                        field: err.field,
                                        rejectedValud: err.rejectedValue,
                                        message: err.defaultMessage
                                ]
                            }
                    ]
            ) as HttpResponse<Map>
        }
    }
}
