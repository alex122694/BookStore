<script setup>
defineProps({
    // 允許覆寫顏色，預設 primary
    color: {
        type: String,
        default: 'primary'
    },
    // 允許覆寫 icon，預設 mdi-plus
    icon: {
        type: String,
        default: 'mdi-plus'
    },
    // 允許設定圓角，預設為 null (由 Vuetify 或樣式決定)
    rounded: {
        type: String,
        default: 'lg'
    }
});

defineEmits(['click']);
</script>

<template>
    <v-btn :color="color" :prepend-icon="icon ? icon : undefined" elevation="2" size="large" :rounded="rounded"
        class="text-body-1 font-weight-bold" @click="$emit('click')">
        <slot>新增</slot>
    </v-btn>
</template>

<style scoped>
/* 統一按鈕樣式微調 */
.v-btn {
    text-transform: none;
    /* 避免全大寫，視需求而定 */
    letter-spacing: 0.5px;
}
</style>
