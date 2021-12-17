package com.webshop.server.model

import org.hibernate.Hibernate
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "goods")
data class GoodsModel(
    @Id
    val id: Int? = null,
    val name: String? = null,
    val count: Int? = null) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as GoodsModel

        return id != null && id == other.id
    }

    override fun hashCode(): Int = 0

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , name = $name , count = $count )"
    }
}
