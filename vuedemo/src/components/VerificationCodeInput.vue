<template>
  <div class="verification-code-input">
    <div class="input-container">
      <input
        v-for="(item, index) in codeLength"
        :key="index"
        ref="inputs"
        v-model="code[index]"
        :maxlength="1"
        :disabled="isCompleted"
        :class="{ 'input-item': true, 'disabled': isCompleted }"
        @input="handleInput(index, $event)"
        @keydown="handleKeydown(index, $event)"
        @focus="handleFocus(index)"
        @paste="handlePaste"
        type="text"
       
        pattern="[0-9]*"
      />
      <!--  inputmode="numeric"可以不要，是手机展示数字键盘用的
          @paste="handlePaste"也去掉-->
    </div>
    
    <div v-if="showMessage" class="message" :class="{ 'success': isSuccess, 'error': !isSuccess }">
      {{ message }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'VerificationCodeInput',
  props: {
    length: {
      type: Number,
      default: 6
    }
  },
  data() {
    return {
      code: Array(this.length).fill(''),
      isCompleted: false,
      showMessage: false,
      message: '',
      isSuccess: false
    }
  },
  computed: {
    codeLength() {
      return this.length
    }
  },
  methods: {
    handleInput(index, event) {
      const value = event.target.value
      
      if (/^[0-9]$/.test(value)) {
        this.code[index] = value
        
        if (index < this.codeLength - 1) {
          this.$nextTick(() => {
            this.$refs.inputs[index + 1].focus()
          })
        }
        
        this.checkCompletion()
      } else {
        this.code[index] = ''
      }
    },
    
    //按删除那个键把焦点移到前一个 
    handleKeydown(index, event) {
      if (event.key === 'Backspace' && !this.code[index] && index > 0) {
        event.preventDefault()//阻止默认事件
        this.$refs.inputs[index - 1].focus()
      }
    },
    
    //这个也可以不要，就是点击的时候默认选中，一般输入了数字，焦点就到下一个了，本身就是没有东西让你复制的
    //这个组件的功能也没啥好复制其中一个数的
    handleFocus(index) {
      if (this.isCompleted) {
        return
      }
      
      this.$refs.inputs[index].select()//选中数字，可以直接复制
    },
    
    // 当用户通过浏览器的用户界面发起“粘贴”动作时，将触发 paste 事件。
    // clipboardData是实验性的，不建议用
    handlePaste(event) {
      if (this.isCompleted) {
        return
      }
      
      event.preventDefault()
      const pasteData = event.clipboardData.getData('text')
      const numbers = pasteData.replace(/[^0-9]/g, '').slice(0, this.codeLength)
      
      for (let i = 0; i < numbers.length; i++) {
        this.code[i] = numbers[i]
      }
      
      this.checkCompletion()
    },
    
    checkCompletion() {
      const isFilled = this.code.every(item => item !== '')
      
      if (isFilled) {
        this.isCompleted = true
        this.verifyCode()
      }
    },
    
    async verifyCode() {
      const codeString = this.code.join('')
      
      try {
        const isValid = await this.mockApiCall(codeString)
        
        if (isValid) {
          this.showSuccess('验证成功！')
          console.log('验证码校验成功:', codeString)
        } else {
          this.showError('验证失败，请重新输入')
          this.resetInput()
        }
      } catch (error) {
        this.showError('验证出错，请重试')
        this.resetInput()
      }
    },
    
    mockApiCall(code) {
      return new Promise((resolve) => {
        setTimeout(() => {
          const correctCodes = {
            4: '1234',
            6: '123456', 
            8: '12345678'
          }
          resolve(code === correctCodes[this.codeLength])
        }, 1000)
      })
    },
    
    showSuccess(msg) {
      this.isSuccess = true
      this.message = msg
      this.showMessage = true
    },
    
    showError(msg) {
      this.isSuccess = false
      this.message = msg
      this.showMessage = true
    },
    
    resetInput() {
      setTimeout(() => {
        this.code = Array(this.codeLength).fill('')
        this.isCompleted = false
        this.showMessage = false
        this.$nextTick(() => {
          this.$refs.inputs[0].focus()
        })
      }, 1500)
    }
  },
  
  //其他的地方应该不需要nextTick
  mounted() {
    this.$nextTick(() => {
      if (this.$refs.inputs && this.$refs.inputs[0]) {
        this.$refs.inputs[0].focus()
      }
    })
  }
}
</script>

<style scoped>
.verification-code-input {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.input-container {
  display: flex;
  gap: 10px;
}

.input-item {
  width: 50px;
  height: 60px;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  outline: none;
  transition: all 0.3s;
}

.input-item:focus {
  border-color: #409eff;
  box-shadow: 0 0 5px rgba(64, 158, 255, 0.3);
}

.input-item.disabled {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
  cursor: not-allowed;
}

.message {
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
}

.message.success {
  background-color: #f0f9ff;
  color: #67c23a;
  border: 1px solid #e1f3d8;
}

.message.error {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
}
</style>