<script setup>
defineProps({
    presets: {
        type: Array,
        default: () => [],
        // Expected format: { title: 'Button Title', icon?: 'mdi-icon', color?: 'primary', handler: Function }
    },
    showClear: {
        type: Boolean,
        default: false
    },
    clearHandler: {
        type: Function,
        default: () => { }
    }
});
</script>

<template>
    <div class="d-flex gap-2 align-center flex-wrap">
        <v-btn v-for="(preset, index) in presets" :key="index" :color="preset.color || 'info'" variant="tonal"
            :prepend-icon="preset.icon || 'mdi-pencil-plus'" @click="preset.handler" class="mr-2">
            {{ preset.title }}
        </v-btn>

        <v-btn v-if="showClear" color="error" variant="tonal" prepend-icon="mdi-delete-sweep" @click="clearHandler">
            清空欄位
        </v-btn>
    </div>
</template>

<style scoped>
.gap-2 {
    gap: 8px;
}
</style>
