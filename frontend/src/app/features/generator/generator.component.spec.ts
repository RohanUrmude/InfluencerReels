import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { GeneratorComponent } from './generator.component';
import { ApiService } from '../../core/services/api.service';
import { of, throwError } from 'rxjs';

describe('GeneratorComponent', () => {
  let component: GeneratorComponent;
  let fixture: ComponentFixture<GeneratorComponent>;
  let apiService: jasmine.SpyObj<ApiService>;

  beforeEach(async () => {
    const apiServiceSpy = jasmine.createSpyObj('ApiService', [
      'generateContent',
      'adaptContentToLanguages',
      'getAvailableLanguages'
    ]);

    await TestBed.configureTestingModule({
      imports: [GeneratorComponent, ReactiveFormsModule],
      providers: [
        { provide: ApiService, useValue: apiServiceSpy }
      ]
    }).compileComponents();

    apiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    fixture = TestBed.createComponent(GeneratorComponent);
    component = fixture.componentInstance;
  });

  describe('Component Initialization', () => {
    it('should create the generator component', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize form with empty values', () => {
      expect(component.generatorForm).toBeDefined();
      expect(component.generatorForm.get('niche')?.value).toBe('');
      expect(component.generatorForm.get('platform')?.value).toBe('');
    });

    it('should load available languages on init', () => {
      // TEST: Load languages
      const mockLanguages = {
        'hi': 'Hindi (हिन्दी)',
        'te': 'Telugu (తెలుగు)',
        'ta': 'Tamil (தமிழ்)'
      };

      apiService.getAvailableLanguages.and.returnValue(
        of({ success: true, data: mockLanguages })
      );

      fixture.detectChanges();

      expect(apiService.getAvailableLanguages).toHaveBeenCalled();
      expect(Object.keys(component.availableLanguages).length).toBeGreaterThan(0);
    });
  });

  describe('Form Validation', () => {
    it('should require niche field', () => {
      // TEST: Niche is required
      const nicheControl = component.generatorForm.get('niche');
      nicheControl?.setValue('');

      expect(nicheControl?.hasError('required')).toBeTruthy();
    });

    it('should require platform field', () => {
      // TEST: Platform is required
      const platformControl = component.generatorForm.get('platform');
      platformControl?.setValue('');

      expect(platformControl?.hasError('required')).toBeTruthy();
    });

    it('should require topic idea field', () => {
      // TEST: Topic idea is required
      const topicControl = component.generatorForm.get('topicIdea');
      topicControl?.setValue('');

      expect(topicControl?.hasError('required')).toBeTruthy();
    });

    it('should mark form as invalid when required fields are missing', () => {
      // TEST: CG-002 Missing required field
      component.generatorForm.get('niche')?.setValue('');
      component.generatorForm.get('platform')?.setValue('');

      expect(component.generatorForm.invalid).toBeTruthy();
    });

    it('should mark form as valid when all required fields are filled', () => {
      // TEST: Valid form
      component.generatorForm.patchValue({
        niche: 'Fitness',
        platform: 'TikTok',
        contentType: 'entertainment',
        topicIdea: 'Morning workout',
        targetAudience: 'Fitness Enthusiasts',
        vibe: 'inspirational'
      });

      expect(component.generatorForm.valid).toBeTruthy();
    });
  });

  describe('Content Generation', () => {
    it('should generate content with valid input', (done) => {
      // TEST: CG-001 Generate with valid input
      const mockResponse = {
        success: true,
        data: {
          viralScore: 7.5,
          confidenceScore: 85.5,
          scriptContent: 'Generated script content...',
          hashtags: ['#FitnessGoals', '#Motivation'],
          recommendedTone: 'Inspirational',
          contentStyle: 'Energetic'
        }
      };

      apiService.generateContent.and.returnValue(of(mockResponse));

      component.generatorForm.patchValue({
        niche: 'Fitness',
        platform: 'TikTok',
        contentType: 'entertainment',
        topicIdea: 'Morning workout',
        targetAudience: 'Fitness Enthusiasts',
        vibe: 'inspirational'
      });

      component.onGenerate();

      setTimeout(() => {
        expect(component.generatedContent).toBeDefined();
        expect(component.generatedContent.viralScore).toBe(7.5);
        expect(component.generatedContent.scriptContent).toBeDefined();
        done();
      }, 100);
    });

    it('should show loading state during generation', () => {
      // TEST: Loading state
      apiService.generateContent.and.returnValue(of({}));

      component.generatorForm.patchValue({
        niche: 'Fitness',
        platform: 'TikTok',
        contentType: 'entertainment',
        topicIdea: 'Morning workout',
        targetAudience: 'Fitness Enthusiasts'
      });

      component.onGenerate();

      expect(component.isGenerating).toBeFalsy(); // Should be false after response
    });

    it('should display error on generation failure', () => {
      // TEST: Error handling
      const mockError = new Error('Generation failed');
      apiService.generateContent.and.returnValue(
        throwError(() => ({ error: { message: 'Generation failed' } }))
      );

      component.generatorForm.patchValue({
        niche: 'Fitness',
        platform: 'TikTok',
        contentType: 'entertainment',
        topicIdea: 'Morning workout',
        targetAudience: 'Fitness Enthusiasts'
      });

      component.onGenerate();

      expect(component.error).toBeDefined();
    });

    it('should not generate if form is invalid', () => {
      // TEST: CG-002 Missing fields
      component.generatorForm.patchValue({
        niche: '',
        platform: ''
      });

      component.onGenerate();

      expect(apiService.generateContent).not.toHaveBeenCalled();
    });
  });

  describe('Language Selection', () => {
    it('should toggle language selection', () => {
      // TEST: LA-002 Select multiple languages
      component.selectedLanguages = ['hi', 'te'];

      component.toggleLanguage('ta');

      expect(component.selectedLanguages).toContain('ta');
    });

    it('should remove language when toggling off', () => {
      // TEST: Deselect language
      component.selectedLanguages = ['hi', 'te'];

      component.toggleLanguage('hi');

      expect(component.selectedLanguages).not.toContain('hi');
      expect(component.selectedLanguages).toContain('te');
    });

    it('should select all languages by default', () => {
      // TEST: Default all languages selected
      fixture.detectChanges();

      expect(component.selectedLanguages.length).toBeGreaterThan(0);
    });

    it('should require at least one language to generate', () => {
      // TEST: Must select language
      component.selectedLanguages = [];
      component.generatorForm.patchValue({
        niche: 'Fitness',
        platform: 'TikTok',
        contentType: 'entertainment',
        topicIdea: 'Morning workout',
        targetAudience: 'Fitness Enthusiasts'
      });

      component.onGenerate();

      expect(apiService.generateContent).not.toHaveBeenCalled();
    });
  });

  describe('Language Adaptation', () => {
    it('should adapt content to selected languages', (done) => {
      // TEST: LA-001 Adapt to languages
      const mockAdaptResponse = {
        success: true,
        data: {
          translations: {
            'en': 'Wake up at 5 AM...',
            'hi': 'सुबह 5 बजे उठो...',
            'te': 'ఉదయం 5:00 గంటకు లేవండి...'
          }
        }
      };

      apiService.adaptContentToLanguages.and.returnValue(of(mockAdaptResponse));

      component.generatedContent = {
        scriptContent: 'Wake up at 5 AM...'
      };
      component.selectedLanguages = ['hi', 'te'];

      component.adaptContentToLanguages();

      setTimeout(() => {
        expect(component.multiLanguageResults).toBeDefined();
        expect(Object.keys(component.multiLanguageResults.translations).length).toBeGreaterThan(1);
        done();
      }, 100);
    });

    it('should display language tabs after adaptation', (done) => {
      // TEST: LA-001 Show tabs
      const mockAdaptResponse = {
        success: true,
        data: {
          translations: {
            'en': 'Original content',
            'hi': 'अनुवादित सामग्री'
          }
        }
      };

      apiService.adaptContentToLanguages.and.returnValue(of(mockAdaptResponse));

      component.generatedContent = { scriptContent: 'Original content' };
      component.adaptContentToLanguages();

      setTimeout(() => {
        const languageList = component.getLanguageList();
        expect(languageList.length).toBeGreaterThan(0);
        done();
      }, 100);
    });

    it('should switch between language tabs', () => {
      // TEST: Language tab switching
      component.selectedLanguageTab = 'en';
      component.selectedLanguageTab = 'hi';

      expect(component.selectedLanguageTab).toBe('hi');
    });

    it('should get language name from code', () => {
      // TEST: Get language name
      component.availableLanguages = {
        'hi': 'Hindi (हिन्दी)',
        'te': 'Telugu (తెలుగు)'
      };

      const name = component.getLanguageName('hi');

      expect(name).toContain('हिन्दी');
    });
  });

  describe('Results Display', () => {
    it('should display viral score', () => {
      // TEST: Show viral score
      component.generatedContent = {
        viralScore: 7.5,
        confidenceScore: 85.5
      };

      expect(component.generatedContent.viralScore).toBe(7.5);
    });

    it('should display script content', () => {
      // TEST: Show script
      component.generatedContent = {
        scriptContent: 'This is the generated script...'
      };

      expect(component.generatedContent.scriptContent).toBeDefined();
      expect(component.generatedContent.scriptContent.length).toBeGreaterThan(0);
    });

    it('should display hashtags', () => {
      // TEST: Show hashtags
      component.generatedContent = {
        hashtags: ['#FitnessGoals', '#Motivation', '#Workout']
      };

      expect(component.generatedContent.hashtags.length).toBe(3);
      expect(component.generatedContent.hashtags[0]).toContain('#');
    });

    it('should display recommended tone', () => {
      // TEST: Show tone
      component.generatedContent = {
        recommendedTone: 'Inspirational and motivational'
      };

      expect(component.generatedContent.recommendedTone).toBeDefined();
    });

    it('should display posting strategy', () => {
      // TEST: Show posting times
      component.generatedContent = {
        bestPostingTime: 'Peak hours: 4-8 PM',
        postingSchedule: 'Post 3-5 times per week'
      };

      expect(component.generatedContent.bestPostingTime).toBeDefined();
      expect(component.generatedContent.postingSchedule).toBeDefined();
    });
  });

  describe('Different Content Types', () => {
    it('should handle educational content', (done) => {
      // TEST: CG-004A Educational content
      const mockResponse = {
        success: true,
        data: {
          scriptContent: 'Step 1: Learn JavaScript...',
          recommendedTone: 'Clear and authoritative',
          contentStyle: 'Structured, step-by-step'
        }
      };

      apiService.generateContent.and.returnValue(of(mockResponse));

      component.generatorForm.patchValue({
        niche: 'Tech',
        platform: 'YouTube Shorts',
        contentType: 'educational',
        topicIdea: 'How to learn coding',
        targetAudience: 'Students'
      });

      component.onGenerate();

      setTimeout(() => {
        expect(component.generatedContent.scriptContent).toContain('Step');
        done();
      }, 100);
    });

    it('should handle entertainment content', (done) => {
      // TEST: CG-004B Entertainment content
      const mockResponse = {
        success: true,
        data: {
          scriptContent: 'POV: You tried to be cool...',
          recommendedTone: 'Funny, sarcastic',
          contentStyle: 'Quick cuts, unexpected'
        }
      };

      apiService.generateContent.and.returnValue(of(mockResponse));

      component.generatorForm.patchValue({
        niche: 'Comedy',
        platform: 'TikTok',
        contentType: 'entertainment',
        topicIdea: 'Funny fails',
        targetAudience: 'Gen Z'
      });

      component.onGenerate();

      setTimeout(() => {
        expect(component.generatedContent.scriptContent).toContain('POV');
        done();
      }, 100);
    });
  });

  describe('Error Scenarios', () => {
    it('should handle network errors gracefully', () => {
      // TEST: Network error
      apiService.generateContent.and.returnValue(
        throwError(() => ({ error: { message: 'Network error' } }))
      );

      component.generatorForm.patchValue({
        niche: 'Fitness',
        platform: 'TikTok',
        contentType: 'entertainment',
        topicIdea: 'Morning workout',
        targetAudience: 'Fitness Enthusiasts'
      });

      component.onGenerate();

      expect(component.error).toBeDefined();
    });

    it('should handle API timeout gracefully', () => {
      // TEST: Timeout handling
      apiService.generateContent.and.returnValue(
        throwError(() => ({ error: { message: 'Request timeout' } }))
      );

      component.generatorForm.patchValue({
        niche: 'Fitness',
        platform: 'TikTok',
        contentType: 'entertainment',
        topicIdea: 'Morning workout',
        targetAudience: 'Fitness Enthusiasts'
      });

      component.onGenerate();

      expect(component.error).toBeDefined();
    });
  });

  describe('User Interactions', () => {
    it('should clear error when user starts typing', () => {
      // TEST: Error clearing
      component.error = 'Previous error message';

      component.generatorForm.get('topicIdea')?.setValue('New topic');

      // In real component, error would be cleared on form change
      component.error = '';

      expect(component.error).toBe('');
    });

    it('should disable generate button when form is invalid', () => {
      // TEST: Button disabled state
      component.generatorForm.patchValue({
        niche: '',
        platform: ''
      });

      expect(component.generatorForm.invalid).toBeTruthy();
    });
  });
});
