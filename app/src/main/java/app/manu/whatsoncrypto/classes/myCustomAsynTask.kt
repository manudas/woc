package app.manu.whatsoncrypto.classes

import android.os.AsyncTask

class myCustomAsynTask {
    private lateinit var _myAsyncMachine: AsyncTask<String, Unit, Any?>
    private val _mOnFinishAsyncMachineFunctions : MutableList<(Any?) -> Any?> = mutableListOf<(Any?) -> Any?>()
    private val _mAsyncCode : MutableList<(Array<out String?>) -> Any?> = mutableListOf<(Array<out String?>) -> Any?>()
    private val _mAsyncResult : MutableList<Any?> = mutableListOf<Any?>()
    private lateinit var _myAsyncMachine_arr : Array<AsyncTask<String, Unit, Any?>>
    private lateinit var  _mOnFinishAsyncMachineFunctions_arr : Array<MutableList<(Any?) -> Any?>>
    private lateinit var  _mAsyncCode_arr : Array<MutableList<(Array<out String?>) -> Any?>>
    private lateinit var _mAsyncResult_arr : Array<MutableList<Any?>>

    public fun resetAsynTask(i: Int? = null, save: Boolean = true) : AsyncTask<String, Unit, Any?> {
        val machine_obj = object: AsyncTask<String, Unit, Any?>() {
            override fun doInBackground(vararg params: String?): Any? {
                if (i == null) {
                    _mAsyncResult.clear()
                    for (function in _mAsyncCode) {
                        _mAsyncResult.add(function(params))
                    }
                    return _mAsyncResult
                }
                else {
                    _mAsyncResult_arr[i].clear()
                    for (function in _mAsyncCode_arr[i]) {
                        _mAsyncResult_arr[i].add(function(params))
                    }
                    return _mAsyncResult_arr[i]
                }
            }

            override fun onPostExecute(result: Any?) {

                if (i == null) {
                    for ((index, function) in _mOnFinishAsyncMachineFunctions.withIndex()) {
                        val function_result = _mAsyncResult.getOrNull(index)
                        function(function_result)
                    }
                }
                else {
                    for ((index, function) in _mOnFinishAsyncMachineFunctions_arr[i].withIndex()) {
                        val function_result = _mAsyncResult_arr[i].getOrNull(index)
                        function(function_result)
                    }
                }
            }
        }

        if (save == true) {
            if (i == null) {
                this._myAsyncMachine = machine_obj
            } else {
                this._myAsyncMachine_arr[i] = machine_obj
            }
        }
        return machine_obj
    }

    public fun initMachine() = resetAsynTask()

    public fun resetOnFinishFunctions(onFinish : List<(Any?) -> Any?>? = null, i: Int? = null) {
        if (i == null) {
            _mOnFinishAsyncMachineFunctions.clear()
            if (onFinish != null) {
                _mOnFinishAsyncMachineFunctions.addAll(onFinish)
            }
        }
        else {
            _mOnFinishAsyncMachineFunctions_arr[i].clear()
            if (onFinish != null) {
                _mOnFinishAsyncMachineFunctions_arr[i].addAll(onFinish)
            }
        }
    }

    public fun addOnFinishFunction(onFinish : (Any?) -> Any?, i: Int? = null) {
        if (i == null) {
            _mOnFinishAsyncMachineFunctions.add(onFinish)
        }
        else {
            _mOnFinishAsyncMachineFunctions_arr[i].add(onFinish)
        }
    }

    public fun addAllOnFinishFunctions(onFinish : List<(Any?) -> Any?>, i: Int? = null) {
        if (i == null) {
            _mOnFinishAsyncMachineFunctions.addAll(onFinish)
        }
        else {
            _mOnFinishAsyncMachineFunctions_arr[i].addAll(onFinish)
        }
    }

    public fun resetCoreFunctions(coreFunctions: List<(Array<out String?>) -> Any?>? = null, i: Int? = null){
        if (i == null) {
            _mAsyncCode.clear()
            if (coreFunctions != null) {
                _mAsyncCode.addAll(coreFunctions)
            }
        }
        else {
            _mAsyncCode_arr[i].clear()
            if (coreFunctions != null) {
                _mAsyncCode_arr[i].addAll(coreFunctions)
            }
        }
    }

    public fun addCoreFunctions(coreFunction: (Array<out String?>) -> Any?, i: Int? = null){
        if (i == null) {
            _mAsyncCode.add(coreFunction)
        }
        else {
            _mAsyncCode_arr[i].add(coreFunction)
        }
    }

    public fun addAllCoreFunctions(coreFunctions: List<(Array<out String?>) -> Any?>, i: Int? = null){
        if (i == null) {
            if (coreFunctions != null) {
                _mAsyncCode.addAll(coreFunctions)
            }
        }
        else {
            if (coreFunctions != null) {
                _mAsyncCode_arr[i].addAll(coreFunctions)
            }
        }
    }

    public fun execute(i: Int? = null) {
        if(this._myAsyncMachine.status == AsyncTask.Status.FINISHED) {
            resetAsynTask(i)
        }
        if (i == null) this._myAsyncMachine.execute()
        else this._myAsyncMachine_arr[i].execute()
    }

    public fun getResult(i: Int? = null) : List<Any?>{
        if (i == null) {
            return this._mAsyncResult
        }
        else {
            return this._mAsyncResult_arr[i]
        }
    }

    public fun initMultiple(number: Int) {

        this._myAsyncMachine_arr = Array(number) { index->
            resetAsynTask(index, save = false)
        }
        this._mOnFinishAsyncMachineFunctions_arr = Array(number) { index ->
            mutableListOf<(Any?) -> Any?>()
        }
        this. _mAsyncCode_arr = Array(number) { index ->
            mutableListOf<(Array<out String?>) -> Any?>()
        }
        this._mAsyncResult_arr = Array(number) {mutableListOf<Any?>()}
    }
}