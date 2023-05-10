package me.dio.creditapplicationsystem.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.creditapplicationsystem.dto.request.CustomerDTO
import me.dio.creditapplicationsystem.dto.request.CustomerUpdateDTO
import me.dio.creditapplicationsystem.entity.Customer
import me.dio.creditapplicationsystem.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc()
@ContextConfiguration()
class CustomerResourceTest {
    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/customers"
    }

    @BeforeEach
    fun setup() = customerRepository.deleteAll()

    @AfterEach
    fun tearDown() = customerRepository.deleteAll()

    @Test
    fun `should create a customer and return 201 status`() {
        val customerDTO: CustomerDTO = buildCustomerDTO()
        val valueAsString = objectMapper.writeValueAsString(customerDTO)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Elon"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Musk"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("18001082067"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("elon@musk.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("12345555"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Rua Tesla"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not save a customer with the same CPF and return status 409`() {
        customerRepository.save(buildCustomerDTO().toEntity())
        val customerDTO: CustomerDTO = buildCustomerDTO()
        val valueAsString = objectMapper.writeValueAsString(customerDTO)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Conflict! Consult the documentation."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(409))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.dao.DataIntegrityViolationException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not save a customer with firstName empty and return status 400`() {
        val customerDTO: CustomerDTO = buildCustomerDTO(firstName = "")
        val valueAsString = objectMapper.writeValueAsString(customerDTO)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .content(valueAsString)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.web.bind.MethodArgumentNotValidException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find a customer by id and return status 200`() {
        val customer: Customer = customerRepository.save(buildCustomerDTO().toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL/${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Elon"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Musk"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("18001082067"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("elon@musk.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("12345555"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Rua Tesla"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find a customer with invalid id and return status 400`() {
        val invalidId: Long = 2L

        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL/$invalidId")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.creditapplicationsystem.exception.BusinessException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should delete a customer by id and return status 204`() {
        val customer: Customer = customerRepository.save(buildCustomerDTO().toEntity())
        mockMvc.perform(
            MockMvcRequestBuilders.delete("$URL/${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNoContent)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not delete a customer by id and return status 400`() {
        val invalidId: Long = Random().nextLong()
        mockMvc.perform(
            MockMvcRequestBuilders.delete("$URL/${invalidId}")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.creditapplicationsystem.exception.BusinessException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should update a customer and return 200`() {
        val customer: Customer = customerRepository.save(buildCustomerDTO().toEntity())
        val customerUpdateDTO: CustomerUpdateDTO = buildCustomerUpdateDTO()
        val valueAsString: String = objectMapper.writeValueAsString(customerUpdateDTO)

        mockMvc.perform(
            MockMvcRequestBuilders.patch("$URL?customerId=${customer.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Elonzin"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Muskinh"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("18001082067"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("elon@musk.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("11122333"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Rua Tesla Model S"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.income").value(5720000.0))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not update a customer with invalid id return status 400`() {
        val invalidId: Long = Random().nextLong()
        val customerUpdateDTO: CustomerUpdateDTO = buildCustomerUpdateDTO()
        val valueAsString: String = objectMapper.writeValueAsString(customerUpdateDTO)

        mockMvc.perform(
            MockMvcRequestBuilders.patch("$URL?customerId=$invalidId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    private fun buildCustomerDTO(
        firstName: String = "Elon",
        lastName: String = "Musk",
        cpf: String = "18001082067",
        email: String = "elon@musk.com",
        password: String = "12345",
        zipCode: String = "12345555",
        street: String = "Rua Tesla",
        income: BigDecimal = BigDecimal.valueOf(2500000.0),
    ) = CustomerDTO(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        password = password,
        zipCode = zipCode,
        street = street,
        income = income,
    )

    private fun buildCustomerUpdateDTO(
        firstName: String = "Elonzin",
        lastName: String = "Muskinh",
        zipCode: String = "11122333",
        street: String = "Rua Tesla Model S",
        income: BigDecimal = BigDecimal.valueOf(5720000.0)
    ): CustomerUpdateDTO = CustomerUpdateDTO(
        firstName = firstName,
        lastName = lastName,
        income = income,
        zipCode = zipCode,
        street = street
    )
}