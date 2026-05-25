-- Update generated_content table column types to accommodate longer AI-generated text
-- Run this SQL script directly against the viralforge_ai database

ALTER TABLE generated_content
  ALTER COLUMN audience_type TYPE TEXT,
  ALTER COLUMN recommended_tone TYPE TEXT,
  ALTER COLUMN content_style TYPE TEXT,
  ALTER COLUMN trend_alignment TYPE TEXT;

-- Verify the changes
\d generated_content
