<template>
  <div class="home">
    <h1>验证码输入组件示例</h1>
    
    <div class="demo-section">
      <h2>6位验证码示例</h2>
      <p class="demo-description">请输入6位数字验证码，正确验证码为：123456</p>
      <VerificationCodeInput :length="6" />
    </div>
    
    <div class="demo-section">
      <h2>4位验证码示例</h2>
      <p class="demo-description">请输入4位数字验证码，正确验证码为：1234</p>
      <VerificationCodeInput :length="4" />
    </div>
    
    <div class="demo-section">
      <h2>8位验证码示例</h2>
      <p class="demo-description">请输入8位数字验证码，正确验证码为：12345678</p>
      <VerificationCodeInput :length="8" />
    </div>
    
    <div class="feature-list">
      <h3>组件特性：</h3>
      <ul>
        <li>支持任意长度的验证码输入</li>
        <li>自动焦点跳转，提升输入体验</li>
        <li>支持粘贴功能</li>
        <li>输入完成后自动验证</li>
        <li>验证成功/失败有明确提示</li>
        <li>验证失败后自动重置</li>
      </ul>
    </div>
  </div>
</template>

<script>
import VerificationCodeInput from '@/components/VerificationCodeInput.vue'

export default {
  name: 'Home',
  components: {
    VerificationCodeInput
  }
}
</script>

<style scoped>
.home {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.demo-section {
  margin: 40px 0;
  padding: 30px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background-color: #fafafa;
}

.demo-section h2 {
  color: #303133;
  margin-bottom: 10px;
}

.demo-description {
  color: #606266;
  margin-bottom: 20px;
  font-size: 14px;
}

.feature-list {
  margin-top: 50px;
  padding: 20px;
  background-color: #f0f9ff;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.feature-list h3 {
  color: #303133;
  margin-bottom: 15px;
}

.feature-list ul {
  text-align: left;
  color: #606266;
  line-height: 1.8;
}

.feature-list li {
  margin-bottom: 8px;
}
</style>