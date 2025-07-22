package com.example.solutionchallenge.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.solutionchallenge.R
import com.example.solutionchallenge.data.FoodItem

class FoodItemAdapter (
    private var list: MutableList<FoodItem>
) : RecyclerView.Adapter<FoodItemAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val nameEdit: EditText  = view.findViewById(R.id.foodName)
        val sugarEdit: EditText = view.findViewById(R.id.foodSugar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_result, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, _position: Int) {
        // 1) 현재 바인딩된 어댑터 포지션
        val pos = holder.bindingAdapterPosition
        if (pos == RecyclerView.NO_POSITION) return

        // 2) 뷰에 현재 데이터 세팅
        val item = list[pos]
        holder.nameEdit.setText(item.name)
        holder.sugarEdit.setText(item.sugar.toString())

        // 3) 이전에 붙은 TextWatcher 지우기 (중복 방지)
        holder.nameEdit.tag?.let { old ->
            (old as? TextWatcher)?.let { holder.nameEdit.removeTextChangedListener(it) }
        }
        holder.sugarEdit.tag?.let { old ->
            (old as? TextWatcher)?.let { holder.sugarEdit.removeTextChangedListener(it) }
        }

        // 4) 새 TextWatcher 붙이기
        val nameWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val newPos = holder.bindingAdapterPosition
                if (newPos != RecyclerView.NO_POSITION) {
                    list[newPos].name = s.toString()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) = Unit
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) = Unit
        }
        holder.nameEdit.addTextChangedListener(nameWatcher)
        holder.nameEdit.tag = nameWatcher

        val sugarWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val newPos = holder.bindingAdapterPosition
                if (newPos != RecyclerView.NO_POSITION) {
                    list[newPos].sugar = s.toString().toFloatOrNull() ?: 0f
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) = Unit
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) = Unit
        }
        holder.sugarEdit.addTextChangedListener(sugarWatcher)
        holder.sugarEdit.tag = sugarWatcher
    }

    override fun getItemCount() = list.size

    // 탭 전환 시 새 데이터로 교체
    fun swapData(newList: MutableList<FoodItem>) {
        list = newList
        notifyDataSetChanged()
    }

    // 버튼으로 빈 항목 추가
    fun addItem() {
        list.add(FoodItem(name = "", amount = 1, sugar = 0f))
        notifyItemInserted(list.size - 1)
    }

    // 최종 리스트를 꺼낼 때
    fun getUpdatedList(): List<FoodItem> = list
}
