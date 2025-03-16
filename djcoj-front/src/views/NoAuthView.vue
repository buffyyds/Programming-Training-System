<template>
  <div class="home">
    <a-result
      status="error"
      title="无权限访问"
      sub-title="您没有权限访问此页面，请联系管理员或返回首页"
    >
      <template #extra>
        <a-button type="primary" @click="$router.push('/')">返回首页</a-button>
      </template>
    </a-result>
  </div>
</template>

<script setup lang="ts">
// 使用 setup 语法糖，自动导出组件
</script>

<style scoped>
.home {
  padding: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 60px);
}
</style>
