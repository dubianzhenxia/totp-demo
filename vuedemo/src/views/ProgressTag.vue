<template>
  <div class="progress-tag-page">
    <div class="header">
      <h1>Vue2 组件示例</h1>
      <div class="navigation">
        <router-link to="/" class="nav-link">验证码输入</router-link>
        <router-link to="/progress-tag" class="nav-link active">进度条标签</router-link>
      </div>
    </div>
    
    <h2>Element UI 进度条标签效果</h2>
    
    <div class="demo-section">
      <h3>简单进度条标签</h3>
      <div class="input-control">
        <span>输入进度 (0-100):</span>
        <el-input 
          v-model="progress" 
          type="number" 
          :min="0" 
          :max="100"
          placeholder="请输入0-100之间的数字"
          style="width: 200px; margin-left: 10px;"
          @input="handleInput"
        ></el-input>
      </div>
      
      <div class="tag-container">
        <el-tag 
          class="progress-tag"
          :style="{ background: tagBackground }"
        >
          进度: {{ progress }}%
        </el-tag>
      </div>
      
      <div class="color-options">
        <h4>选择颜色:</h4>
        <div class="color-buttons">
          <el-button 
            v-for="color in colorOptions" 
            :key="color.name"
            :type="selectedColor === color.name ? 'primary' : 'default'"
            @click="selectColor(color.name)"
            size="small"
          >
            {{ color.label }}
          </el-button>
        </div>
      </div>
    </div>
    
    <div class="demo-section">
      <h3>实现原理</h3>
      <div class="explanation">
        <p>使用 CSS <code>linear-gradient</code> 实现从左到右的背景色渐变效果：</p>
        <pre><code>background: linear-gradient(to right, 
  {{ selectedColorValue }} {{ progress }}%, 
  #EBEEF5 {{ progress }}%
);</code></pre>
        <ul>
          <li><strong>渐变方向</strong>: <code>to right</code> 表示从左到右</li>
          <li><strong>颜色停止点</strong>: 第一个颜色在进度百分比处停止，第二个颜色从同一位置开始</li>
          <li><strong>动态更新</strong>: 通过 Vue 数据绑定实时更新渐变位置</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProgressTag',
  data() {
    return {
      progress: 50,
      selectedColor: 'blue'
    }
  },
  computed: {
    colorOptions() {
      return [
        { name: 'blue', label: '蓝色', value: '#409EFF' },
        { name: 'green', label: '绿色', value: '#67C23A' },
        { name: 'orange', label: '橙色', value: '#E6A23C' },
        { name: 'red', label: '红色', value: '#F56C6C' },
        { name: 'gray', label: '灰色', value: '#909399' }
      ]
    },
    
    selectedColorValue() {
      const color = this.colorOptions.find(c => c.name === this.selectedColor)
      return color ? color.value : '#409EFF'
    },
    
    tagBackground() {
      return `linear-gradient(to right, ${this.selectedColorValue} ${this.progress}%, #EBEEF5 ${this.progress}%)`
    }
  },
  methods: {
    handleInput(value) {
      // 确保输入值在0-100范围内
      if (value === '') {
        this.progress = 0
      } else {
        const numValue = parseInt(value)
        if (!isNaN(numValue)) {
          this.progress = Math.max(0, Math.min(100, numValue))
        }
      }
    },
    
    selectColor(colorName) {
      this.selectedColor = colorName
    }
  }
}
</script>

<style scoped>
.progress-tag-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ebeef5;
}

.header h1 {
  color: #303133;
  margin-bottom: 20px;
}

.navigation {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.nav-link {
  padding: 10px 20px;
  text-decoration: none;
  color: #606266;
  border: 2px solid #dcdfe6;
  border-radius: 6px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.nav-link:hover,
.nav-link.active {
  color: #409eff;
  border-color: #409eff;
  background-color: #f0f9ff;
}

.demo-section {
  margin: 40px 0;
  padding: 30px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background-color: #fafafa;
}

.demo-section h2,
.demo-section h3 {
  color: #303133;
  margin-bottom: 20px;
}

.input-control {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
}

.input-control span {
  color: #606266;
  font-weight: 500;
}

.tag-container {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.progress-tag {
  min-width: 150px;
  text-align: center;
  font-weight: bold;
  font-size: 16px;
  border: none !important;
  transition: background 0.3s ease;
}

.color-options {
  text-align: center;
}

.color-options h4 {
  color: #606266;
  margin-bottom: 15px;
}

.color-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.explanation {
  background: white;
  padding: 20px;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
}

.explanation p {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 15px;
}

.explanation ul {
  color: #606266;
  line-height: 1.8;
}

.explanation li {
  margin-bottom: 8px;
}

.explanation code {
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  color: #f56c6c;
}

.explanation pre {
  background: #f4f4f5;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 15px 0;
}

.explanation pre code {
  background: none;
  color: #303133;
  padding: 0;
}
</style>