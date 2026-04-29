package com.luohuo.game.drawing;

import com.luohuo.game.entity.DrawingWord;
import com.luohuo.game.mapper.DrawingWordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordService {

    private final DrawingWordMapper wordMapper;

    public DrawingWord getWordById(Long id) {
        return wordMapper.selectById(id);
    }

    public List<DrawingWord> getAllWords() {
        return wordMapper.selectList(null);
    }

    public List<DrawingWord> getWordsByCategory(String category) {
        return wordMapper.selectList(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(DrawingWord.class)
                .eq(DrawingWord::getCategory, category)
        );
    }

    public DrawingWord addWord(DrawingWord word) {
        wordMapper.insert(word);
        return word;
    }

    public void deleteWord(Long id) {
        wordMapper.deleteById(id);
    }

    public DrawingWord updateWord(DrawingWord word) {
        wordMapper.updateById(word);
        return word;
    }
}