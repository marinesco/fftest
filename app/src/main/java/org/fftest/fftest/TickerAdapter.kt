package org.fftest.fftest

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.holder_ticker_recyclerview.view.*
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * Created by Marina Zhdanova
 * Contact by email: m.zhdanova@rambler.ru | telegram: t.me/marinesco
 **/

class TickerAdapter(private var list: MutableList<TickerModel>) : RecyclerView.Adapter<TickerAdapter.Holder>() {

    companion object {
        const val FADE_ANIMATION_DURATION = 1300L
        const val UPDATE_ITEM_PAYLOAD = "update_item_payload"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.holder_ticker_recyclerview, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            when (payloads[0]) {
                UPDATE_ITEM_PAYLOAD -> {
                    holder.isUpdated = true
                }
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(list[position])
    }

    override fun onViewDetachedFromWindow(holder: Holder) {
        super.onViewDetachedFromWindow(holder)
        holder.unbind()
    }

    fun updateItem(item: TickerModel) {
        for (i in 0 until list.size) {
            if (list[i].c == item.c) {
                if (item.isUpdated()) {
                    with (list[i]) {
                        this.pcp = item.pcp
                        this.ltp = item.ltp
                        this.chg = item.chg
                    }
                    notifyItemChanged(i, UPDATE_ITEM_PAYLOAD)
                }
                return
            }
        }

        // If item was not found in the ticker list then add it
        list.add(item)
        notifyItemInserted(list.size - 1)
    }

    override fun getItemCount(): Int = list.size

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private var animatorSet: AnimatorSet? = null
        private val c = view.holder_ticker_c_tv
        private val pcp = view.holder_ticker_pcp_tv
        private val ltr_name = view.holder_ticker_ltr_name_tv
        private val ltp_cng = view.holder_ticker_ltp_cng_tv
        var isUpdated: Boolean = false

        fun bind(data: TickerModel) {
            animatorSet?.cancel()
            this.c.text = data.c
            data.pcp?.let {
                this.pcp.text = String.format("%s%%", getFormattedValue(it, null), it)
                when {
                    it == 0.0 -> {
                        this.pcp.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorText))
                        this.pcp.backgroundTintList = ContextCompat.getColorStateList(itemView.context, android.R.color.white)
                    }
                    it > 0.0 -> {
                        val positiveColor = ContextCompat.getColor(itemView.context, R.color.colorGreen)
                        this.pcp.setTextColor(positiveColor)
                        if (isUpdated) {
                            animateTextView(this.pcp, Color.WHITE, positiveColor)
                            isUpdated = false
                        }
                    }
                    it < 0.0 -> {
                        val negativeColor = ContextCompat.getColor(itemView.context, R.color.colorRed)
                        this.pcp.setTextColor(negativeColor)
                        if (isUpdated) {
                            animateTextView(this.pcp, Color.WHITE, negativeColor)
                            isUpdated = false
                        }
                    }
                }
            }

            if (data.ltr != null && data.name != null) {
                this.ltr_name.text = String.format("%s | %s", data.ltr, data.name)
            }

            if (data.ltp != null && data.chg != null) {
                this.ltp_cng.text = String.format("%s ( %s%% )", getFormattedValue(data.ltp, data.min_step, false), getFormattedValue(data.chg, data.min_step))
            }
        }
        
        fun unbind() {
            animatorSet?.cancel()
        }

        /**
         * Animate textview's background tint and text color changing
         * **/

        private fun animateTextView(textView: TextView, @ColorInt fromColorInt: Int, @ColorInt toColorInt: Int) {
            val animatorForBackground = ValueAnimator.ofArgb(fromColorInt, toColorInt).apply {
                repeatMode = ValueAnimator.REVERSE
                repeatCount = 1
            }

            animatorForBackground.addUpdateListener { animation ->
                val color = animation.animatedValue as Int
                textView.background?.setTint(color)
            }

            animatorForBackground.addListener(object : Animator.AnimatorListener {
                override fun onAnimationCancel(animation: Animator?) {
                    animatorForBackground.removeAllUpdateListeners()
                    textView.background?.setTint(fromColorInt)
                }
                override fun onAnimationRepeat(animation: Animator?) = Unit
                override fun onAnimationStart(animation: Animator?) = Unit
                override fun onAnimationEnd(animation: Animator?) = Unit
            })

            val animatorForTextColor = ObjectAnimator.ofArgb(textView, "textColor", toColorInt, fromColorInt).apply {
                repeatMode = ValueAnimator.REVERSE
                repeatCount = 1
            }

            animatorForTextColor.addListener(object : Animator.AnimatorListener {
                override fun onAnimationCancel(animation: Animator?) {
                    textView.setTextColor(toColorInt)
                }
                override fun onAnimationRepeat(animation: Animator?) = Unit
                override fun onAnimationStart(animation: Animator?) = Unit
                override fun onAnimationEnd(animation: Animator?) = Unit
            })

            animatorSet = AnimatorSet().apply {
                duration = FADE_ANIMATION_DURATION
                play(animatorForBackground).with(animatorForTextColor)
                start()
            }
        }

        /**
         * Format Double value
         * @param usePositivePrefix set true to insert "+" for positive numbers
         **/

        private fun getFormattedValue(value: Double?, min_step: String?, usePositivePrefix: Boolean = true): String {
            val format = if (min_step.isNullOrEmpty()) {
                DecimalFormat("0.##", DecimalFormatSymbols(Locale.US))
            } else {
                val plainString = BigDecimal(min_step).toPlainString()
                DecimalFormat(plainString.replaceAfter("1", "").replace("1", "#"), DecimalFormatSymbols(Locale.US))
            }

            if (usePositivePrefix && value != 0.0) {
                format.positivePrefix = "+"
            }

            return format.format(value)
        }
    }
}