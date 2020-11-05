package org.fftest.fftest

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Marina Zhdanova
 * Contact by email: m.zhdanova@rambler.ru | telegram: t.me/marinesco
 **/

@Keep
data class TickerQModel(
    @SerializedName("q")
    @Expose val data: List<TickerModel>
)

/**
 * @param c Тикер
 * @param pcp Изменение в процентах относительно цены закрытия предыдущей торговой сессии
 * @param ltr Биржа последней сделки
 * @param name Название бумаги
 * @param ltp Цена последней сделки
 * @param chg Изменение цены последней сделки в пунктах относительно цены закрытия предыдущей торговой сессии
 * @param min_step Минимальный шаг цены
 * */

@Keep
data class TickerModel(
    @Expose val c: String,
    @Expose var pcp: Double?,
    @Expose val ltr: String?,
    @Expose val name: String?,
    @Expose var ltp: Double?,
    @Expose var chg: Double?,
    @Expose val min_step: String?
) {
    fun isUpdated(): Boolean {
        return pcp != null && ltp != null && chg != null
    }
}