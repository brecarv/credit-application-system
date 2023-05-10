package me.dio.creditapplicationsystem.repository

import me.dio.creditapplicationsystem.entity.Address
import me.dio.creditapplicationsystem.entity.Credit
import me.dio.creditapplicationsystem.entity.Customer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.*

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CreditRepositoryTest {
    @Autowired
    lateinit var creditRepository: CreditRepository

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    private lateinit var customer: Customer
    private lateinit var creditA: Credit
    private lateinit var creditB: Credit

    @BeforeEach
    fun setup() {
        customer = testEntityManager.persist(buildCustomer())
        creditA = testEntityManager.persist(buildCredit(customer = customer))
        creditB = testEntityManager.persist(buildCredit(customer = customer))
    }

    @Test
    fun `should find credit by credit code`() {
        //given
        val creditCodeA = UUID.fromString("a42b10b8-27b4-4885-95dd-12df406cb911")
        val creditCodeB = UUID.fromString("cfaaa813-fa8d-4125-bef1-7f8c479aabc2")
        creditA.creditCode = creditCodeA
        creditB.creditCode = creditCodeB
        //when
        val fakeCreditA: Credit = creditRepository.findByCreditCode(creditCodeA)!!
        val fakeCreditB: Credit = creditRepository.findByCreditCode(creditCodeB)!!
        //then
        Assertions.assertThat(fakeCreditA).isNotNull
        Assertions.assertThat(fakeCreditB).isNotNull
        Assertions.assertThat(fakeCreditA).isSameAs(creditA)
        Assertions.assertThat(fakeCreditB).isSameAs(creditB)
    }

    @Test
    fun `should find all credits by customer id`() {
        //given
        val customerId: Long = 1L
        //when
        val creditList: List<Credit> = creditRepository.findAllByCustomerId(customerId)
        //then
        Assertions.assertThat(creditList).isNotEmpty
        Assertions.assertThat(creditList.size).isEqualTo(2)
        Assertions.assertThat(creditList).contains(creditA, creditB)
    }

    private fun buildCredit(
        creditValue: BigDecimal = BigDecimal.valueOf(500.00),
        dayFirstInstallment: LocalDate = LocalDate.of(2023, Month.APRIL, 22),
        numberOfInstallments: Int = 5,
        customer: Customer
    ): Credit = Credit(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customer = customer
    )

    private fun buildCustomer(
        firstName: String = "Elon",
        lastName: String = "Musk",
        cpf: String = "18001082067",
        email: String = "elon@musk.com",
        password: String = "12345",
        zipCode: String = "12345555",
        street: String = "Rua Tesla",
        income: BigDecimal = BigDecimal.valueOf(2500000.0),
    ) = Customer(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        password = password,
        address = Address(
            zipCode = zipCode,
            street = street
        ),
        income = income,
    )
}