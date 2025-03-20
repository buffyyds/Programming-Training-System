<template>
  <div
    id="code-editor"
    ref="editorRef"
    style="min-height: 400px; height: 60vh"
  />
  <!--  <a-button @click="fillValue">填充值</a-button>-->
</template>

<script setup lang="ts">
import { onMounted, ref, toRaw, withDefaults, defineProps, watch } from "vue";
import * as monaco from "monaco-editor";

/**
 * 定义组件属性类型
 */
interface Props {
  value?: string;
  language?: string;
  handleChange?: (value: string) => void;
}

/**
 * 给组件指定初始值
 */
const props = withDefaults(defineProps<Props>(), {
  value: "",
  language: "java",
  handleChange: (value: string) => {
    // 默认的变更处理函数
    console.log("Code changed:", value);
  },
});

const editorRef = ref<HTMLElement>();
let editor: monaco.editor.IStandaloneCodeEditor;

// const fillValue = () => {
//   if (!editor.value) {
//     return;
//   }
//   // 改变值
//   toRaw(editor.value).setValue("新的值");
// };

// 监听语言变化
watch(
  () => props.language,
  (newLang) => {
    if (editor) {
      // 先清空编辑器内容
      editor.setValue("");
      // 设置新的语言
      monaco.editor.setModelLanguage(editor.getModel()!, newLang);
      // 设置新的内容
      editor.setValue(props.value);
    }
  }
);

// 监听值变化
watch(
  () => props.value,
  (newValue) => {
    if (editor && editor.getValue() !== newValue) {
      editor.setValue(newValue);
    }
  }
);

onMounted(() => {
  if (editorRef.value) {
    editor = monaco.editor.create(editorRef.value, {
      value: props.value,
      language: props.language,
      theme: "vs-dark",
      automaticLayout: true,
      minimap: {
        enabled: false,
      },
      scrollBeyondLastLine: false,
      fontSize: 14,
      tabSize: 4,
      insertSpaces: true,
      wordWrap: "on",
    });

    editor.onDidChangeModelContent(() => {
      props.handleChange(editor.getValue());
    });
  }
});
</script>

<style scoped></style>
